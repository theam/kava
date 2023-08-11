package com.theagilemonkeys.kava.utils.kotlin.async

import JavaImplProcessor
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class JavaImplProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return JavaImplProcessor(environment.codeGenerator, environment.logger)
    }
}
