package com.theagilemonkeys.kava.utils.kotlin.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JavaImpl(
    val classSuffix: String = "",
    val library: Library = Library.COMPLETABLE_FUTURE,
    val defaultScope: KClass<out CoroutineScope> = GlobalScope::class,
)

enum class Library {
    RXJAVA_3,
    COMPLETABLE_FUTURE
}
