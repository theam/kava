# kava

Kava is a library that uses KSP and KotlinPoet to generate Java wrappers based on Kotlin suspend functions, as these are not compatible in Java by default. We're opening this project to the community so everyone can contribute in this common issue when trying to use Kotlin from Java.

# The objective

To have a class level annotation (currently called @JavaImpl) that will detect Kotlin `suspend` functions and will generate another class that wraps every function with a Java compatible wrapper (`CompletableFuture` for now)

The new class will be generated in you sample project (the one that uses the annotation) in the `build/generated/ksp/main/kotlin` directory. This class is generated when running `gradle build`.

Official docs from the used tools can be found below:
1. [KSP docs](https://kotlinlang.org/docs/ksp-quickstart.html)
2. [KotlinPoet](https://github.com/square/kotlinpoet)
   
# Importing it into your project

From your project, you can import your library using `gradle` like this:

```gradle
plugins {
    kotlin("jvm") version "1.8.22"
    id("com.google.devtools.ksp") version "1.8.22-1.0.11"
    kotlin("plugin.serialization") version "1.8.22"
}

dependencies {
    implementation(project(mapOf("path" to ":kotlin")))
    implementation(project(":kotlin"))
    ksp(project(":kotlin"))
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.22"))
    }
}
```

**Note:** This is an example if the sample project is in the same folder as this Kava `kotlin` module.
