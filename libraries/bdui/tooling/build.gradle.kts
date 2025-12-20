plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.libraries.bdui.contract)
    implementation(projects.libraries.bdui.runtime)
    implementation(libs.kotlinx.coroutines.core)
}
