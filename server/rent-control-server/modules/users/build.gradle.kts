plugins {
    alias(libs.plugins.kotlinJvm)
}

dependencies {
    implementation(project(":server:rent-control-server:foundation"))
    implementation(libs.kotlinx.serialization.json)
}
