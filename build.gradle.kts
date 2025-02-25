plugins {
    application
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.teaguild"
version = "0.0.1"
File("VERSION").writeText(version.toString())

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.tgbotapi)
    implementation(libs.ktscheduler)
    implementation(libs.kotlinx.serialization.json)
    runtimeOnly(libs.logback.classic)
}

kotlin {
    jvmToolchain(23)
    sourceSets.all {
        languageSettings.optIn("dev.inmo.tgbotapi.utils.PreviewFeature")
    }
}

application.mainClass = "dev.teaguild.thoughtsntea.MainKt"
