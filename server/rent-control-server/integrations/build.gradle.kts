plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":server:rent-control-server:modules:auth"))
    implementation(libs.ktor.serverCore)
    implementation(libs.logback)
}
