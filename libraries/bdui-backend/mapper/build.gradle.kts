plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.libraries.bduiBackend.core)
    implementation(projects.libraries.bduiBackend.dsl)
    implementation(projects.server.bduiContract)
    testImplementation(libs.kotlin.test)
}
