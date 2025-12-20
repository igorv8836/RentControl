plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.libraries.bduiBackend.core)
    testImplementation(libs.kotlin.test)
}
