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
    implementation(projects.libraries.bduiBackend.dsl)
    implementation(projects.libraries.bduiBackend.contract)
    implementation("org.reflections:reflections:0.9.12")
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlin.test)
}
