package com.theagilemonkeys.kava.utils.kotlin.async.generator

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import kotlinx.coroutines.CoroutineScope
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

class CompletableFutureReplacementGenerator(
    private val defaultScope: KClass<*>,
) : ReplacementGenerator() {
    override fun initialise(fileSpec: FileSpec.Builder) {
        fileSpec.addImport("kotlinx.coroutines.future", "future")
    }

    override fun initialise(typeSpec: TypeSpec.Builder) {
        if (defaultScope.objectInstance != null) {
            typeSpec.addProperty(
                PropertySpec.builder(
                    "scope",
                    CoroutineScope::class
                )
                    .initializer("scope")
                    .build()
            )
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        .addParameter("scope", defaultScope.asTypeName())
                        .build()
                )
        } else {
            typeSpec.addProperty(
                PropertySpec.builder(
                    "scope",
                    CoroutineScope::class
                )
                    .initializer("%T", defaultScope)
                    .build()
            )
        }
    }

    override fun suspendFunction(
        name: String,
        parameters: List<ParameterSpec>,
        returnTypeName: TypeName
    ): FunSpec = FunSpec.builder(name)
        .addParameters(parameters)
        .returns(futureType.parameterizedBy(Unit::class.asTypeName()))
        .beginControlFlow("return %T.future {", defaultScope)
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
        .beginControlFlow("return %T.future {", defaultScope)
        .addCode("%L.toList()", wrappedFunctionCall(name, parameters))
        .endControlFlow()
        .build()

    companion object {
        private val futureType = CompletableFuture::class.asTypeName()
    }
}