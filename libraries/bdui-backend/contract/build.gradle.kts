plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    testImplementation(libs.kotlin.test)
}
