plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "org.igorv8836.rentcontrol"
version = "1.0.0"
application {
    mainClass.set("org.igorv8836.rentcontrol.ApplicationKt")
    
    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(projects.libraries.bduiBackend.core)
    implementation(projects.libraries.bduiBackend.dsl)
    implementation(projects.libraries.bduiBackend.mapper)
    implementation(projects.libraries.bduiBackend.renderer)
    implementation(projects.libraries.bduiBackend.data)
    implementation(projects.libraries.bduiBackend.runtime)
    implementation(projects.libraries.bdui.contract)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
