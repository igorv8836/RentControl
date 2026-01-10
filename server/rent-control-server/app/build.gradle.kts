plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinxSerialization)
    application
}

application {
    mainClass.set("org.igorv8836.rentcontrol.server.app.RentControlApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":server:rent-control-server:foundation"))
    implementation(project(":server:rent-control-server:integrations"))
    implementation(project(":server:rent-control-server:modules:auth"))
    implementation(project(":server:rent-control-server:modules:me"))
    implementation(project(":server:rent-control-server:modules:objects"))
    implementation(project(":server:rent-control-server:modules:users"))

    implementation(libs.logback)
    implementation(libs.ktor.serverNetty)
}
