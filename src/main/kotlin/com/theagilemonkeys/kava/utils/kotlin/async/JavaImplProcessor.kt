import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.theagilemonkeys.kava.utils.kotlin.async.JavaImpl
import com.theagilemonkeys.kava.utils.kotlin.async.Library
import com.theagilemonkeys.kava.utils.kotlin.async.generator.CompletableFutureReplacementGenerator
import com.theagilemonkeys.kava.utils.kotlin.async.generator.RxJavaReplacementGenerator
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.util.Collections.emptyList

class JavaImplProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getSymbolsWithAnnotation(JavaImpl::class.qualifiedName!!).forEach { symbol ->
            when (symbol) {
                is KSClassDeclaration -> generateWrapperClass(symbol)
                else -> logger.error("@JavaImpl must be used on classes only", symbol)
            }
        }
        return emptyList()
    }

    @OptIn(KspExperimental::class)
    private fun generateWrapperClass(symbol: KSClassDeclaration) {
        val javaImpl = symbol.getAnnotationsByType(JavaImpl::class).first()
        val defaultScope = javaImpl.defaultScope
        val generator = when(javaImpl.library) {
            Library.COMPLETABLE_FUTURE -> CompletableFutureReplacementGenerator(defaultScope)
            Library.RXJAVA_3 -> RxJavaReplacementGenerator()
        }
        val fileSpec = generator.buildFileSpec(symbol, javaImpl.classSuffix)
        val file = codeGenerator.createNewFile(Dependencies.ALL_FILES, fileSpec.packageName, fileSpec.name)
        fileSpec.writeTo(PrintWriter(BufferedWriter(OutputStreamWriter(file))))
    }
}
