package com.theagilemonkeys.kava.utils.kotlin.async

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class JavaImpl(
    val classSuffix: String = "",
    val library: Library = Library.COMPLETABLE_FUTURE,
)

enum class Library {
    RXJAVA_3,
    COMPLETABLE_FUTURE
}