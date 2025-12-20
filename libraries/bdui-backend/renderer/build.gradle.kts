plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.libraries.bduiBackend.core)
    implementation(projects.libraries.bdui.contract)
    testImplementation(libs.kotlin.test)
}
