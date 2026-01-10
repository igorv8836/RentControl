plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    implementation(project(":server:rent-control-server:foundation"))
    implementation(project(":server:rent-control-server:modules:users"))

    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}

