import com.theagilemonkeys.kava.utils.kotlin.async.JavaImpl
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.Collections.emptyList

class JavaImplProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
): SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(JavaImpl::class.qualifiedName!!).forEach { symbol ->
            when (symbol) {
                is KSClassDeclaration -> generateWrapperClass(symbol)
                else -> logger.error("@JavaImpl must be used on classes only", symbol)
            }
        }
        return emptyList()
    }

    private fun generateWrapperClass(symbol: KSClassDeclaration) {
        val packageName = symbol.packageName.asString()
        val oldClassName = symbol.simpleName.asString()
        val newClassName = "${oldClassName}Java"
        val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, packageName, newClassName)
        val classBuilder = TypeSpec.classBuilder(newClassName)

        symbol.declarations.filterIsInstance<KSFunctionDeclaration>().forEach { function ->
            if (function.modifiers.contains(Modifier.PUBLIC)) {
                val generatedFun = handleSuspendFunction(function) ?: return@forEach
                classBuilder.addFunction(generatedFun)
            }
        }

        val kotlinFile = FileSpec.builder(packageName, newClassName)
            .addType(classBuilder.build())
            .build()

        kotlinFile.writeTo(PrintWriter(BufferedWriter(OutputStreamWriter(file))))
    }

    private fun handleSuspendFunction(symbol: KSFunctionDeclaration): FunSpec? {
        if (!symbol.modifiers.contains(Modifier.SUSPEND)) return null

        val originalFuncName = "${symbol.containingFile?.fileName?.removeSuffix(".kt")}.${symbol.simpleName.asString()}"
        val newFuncName = "${symbol.simpleName.asString()}Java"

        val params = symbol.parameters.map { ParameterSpec(it.name!!.asString(), ClassName.bestGuess(it.type.resolve().declaration.qualifiedName!!.asString())) }
        val returnType = symbol.returnType?.resolve()?.declaration?.qualifiedName?.asString()
        val funcBuilder = FunSpec.builder(newFuncName)
            .addModifiers(KModifier.PUBLIC)

        params.forEach { funcBuilder.addParameter(it) }

        val completableFutureClass = ClassName("java.util.concurrent", "CompletableFuture")
        funcBuilder.returns(completableFutureClass.parameterizedBy(ClassName.bestGuess(returnType ?: "java.lang.Void")))

        val funcCallParams = params.joinToString(", ") { it.name }
        if (returnType == "kotlin.Unit") {
            funcBuilder.addStatement("return $completableFutureClass.runAsync(Runnable { kotlinx.coroutines.runBlocking { $originalFuncName($funcCallParams) } })")
        } else {
            funcBuilder.addStatement("return $completableFutureClass.supplyAsync(kotlinx.coroutines.runBlocking { $originalFuncName($funcCallParams) })")
        }

        return funcBuilder.build()
    }
}
