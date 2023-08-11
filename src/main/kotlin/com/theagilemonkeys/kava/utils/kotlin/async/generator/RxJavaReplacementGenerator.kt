package com.theagilemonkeys.kava.utils.kotlin.async.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single

class RxJavaReplacementGenerator : ReplacementGenerator() {
    override fun initialise(fileSpec: FileSpec.Builder) {
        fileSpec.addImport(
            "kotlinx.coroutines.rx3",
            "asFlowable",
            "rxMaybe",
            "rxSingle",
        )
    }

    override fun suspendFunction(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName
    ): FunSpec = when(returnTypeName.isNullable){
        true -> nullableFunction(name, parameters, returnTypeName)
        false -> nonNullFunction(name, parameters, returnTypeName)
    }

    private fun nullableFunction(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName
    ): FunSpec = FunSpec.builder(name)
        .returns(Maybe::class.asTypeName().parameterizedBy(returnTypeName))
        .addParameters(parameters)
        .beginControlFlow("return rxMaybe {")
        .addCode(wrappedFunctionCall(name, parameters))
        .endControlFlow()
        .build()

    private fun nonNullFunction(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName
    ): FunSpec = FunSpec.builder(name)
        .returns(Single::class.asTypeName().parameterizedBy(returnTypeName))
        .addParameters(parameters)
        .beginControlFlow("return rxSingle {")
        .addCode(wrappedFunctionCall(name, parameters))
        .endControlFlow()
        .build()

    override fun flow(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName
    ): FunSpec = FunSpec.builder(name)
        .returns(Single::class.asTypeName().parameterizedBy(returnTypeName))
        .addParameters(parameters)
        .addCode("%L.asFlowable()", wrappedFunctionCall(name, parameters))
        .build()
}