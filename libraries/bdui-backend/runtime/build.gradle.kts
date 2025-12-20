plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.libraries.bduiBackend.core)
    implementation(projects.libraries.bduiBackend.mapper)
    implementation(projects.libraries.bduiBackend.renderer)
    implementation(projects.libraries.bduiBackend.data)
    implementation(projects.libraries.bduiBackend.dsl)
    implementation(projects.server.bduiContract)
    testImplementation(libs.kotlin.test)
}
