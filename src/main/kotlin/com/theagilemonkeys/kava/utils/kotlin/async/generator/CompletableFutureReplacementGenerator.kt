package com.theagilemonkeys.kava.utils.kotlin.async.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.coroutines.GlobalScope
import java.util.concurrent.CompletableFuture

class CompletableFutureReplacementGenerator : ReplacementGenerator() {
    override fun initialise(fileSpec: FileSpec.Builder) {
        fileSpec.addImport("kotlinx.coroutines.future", "future")
    }

    override fun suspendFunction(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName
    ): FunSpec = FunSpec.builder(name)
        .addParameters(parameters)
        .returns(futureType.parameterizedBy(Unit::class.asTypeName()))
        .beginControlFlow("return %T.future {", GlobalScope::class)
        .addCode(wrappedFunctionCall(name, parameters))
        .endControlFlow()
        .build()

    override fun flow(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName
    ): FunSpec = FunSpec.builder(name)
        .addParameters(parameters)
        .returns(futureType.parameterizedBy(Unit::class.asTypeName()))
        .beginControlFlow("return %T.future {", GlobalScope::class)
        .addCode("%L.toList()", wrappedFunctionCall(name, parameters))
        .endControlFlow()
        .build()

    companion object {
        private val futureType = CompletableFuture::class.asTypeName()
    }
}