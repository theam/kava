package com.theagilemonkeys.kava.utils.kotlin.async.generator

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

abstract class ReplacementGenerator {
    fun buildFileSpec(symbol: KSClassDeclaration, suffix: String): FileSpec {
        val className = symbol.toClassName()
        val packageName = symbol.packageName.asString()
        val oldClassName = symbol.simpleName.asString()
        val newClassName = "${oldClassName}$suffix"

        val fileBuilder = FileSpec.builder(packageName, newClassName)

        initialise(fileBuilder)

        val classBuilder = TypeSpec.classBuilder(newClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("wrapped", className)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("wrapped", className)
                    .initializer("wrapped")
                    .build()
            )

        classBuilder.addFunctions(
            symbol.declarations.filterIsInstance<KSFunctionDeclaration>()
                .mapNotNull { function -> wrapFunction(function) }
                .toList()
        )
        return fileBuilder.build()
    }

    private fun wrapFunction(symbol: KSFunctionDeclaration): FunSpec? {
        val funcName = symbol.simpleName.asString()
        val params = symbol.parameters.map { it.toParameterSpec() }
        val returnType = symbol.returnType?.toTypeName() ?: unitClass

        return when {
            !symbol.modifiers.contains(Modifier.SUSPEND) -> null
            symbol.modifiers.contains(Modifier.PUBLIC) -> suspendFunction(funcName, params, returnType)
            // todo check for flow
            else -> null
        }
    }

    abstract fun initialise(fileSpec: FileSpec.Builder)

    protected abstract fun suspendFunction(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName,
    ): FunSpec

    protected abstract fun flow(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName,
    ): FunSpec

    protected fun wrappedFunctionCall(
        name: String,
        parameters: List<ParameterSpec>
    ): CodeBlock {
        val funcCallParams = parameters.fold(CodeBlock.builder()) { codeBlock, param ->
            codeBlock.addStatement("%N = %N,", param.name, param.name)
        }
        return CodeBlock.of("wrapped.%N(%L)", name, funcCallParams)
    }

    private fun KSValueParameter.toParameterSpec() = ParameterSpec(
        name!!.asString(),
        type.toTypeName()
    )

    companion object {
        val unitClass = Unit::class.asTypeName()
    }
}