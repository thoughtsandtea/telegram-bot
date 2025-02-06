plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.teaguild"
version = "0.0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation(libs.tgbotapi)
    implementation(libs.ktscheduler)
    implementation(libs.kotlin.serialization.json)
}

kotlin.jvmToolchain(17)
