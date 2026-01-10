plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinxSerialization)
}

dependencies {
    api(libs.ktor.serverCore)
    api(libs.ktor.serverContentNegotiation)
    api(libs.ktor.serializationKotlinxJson)
    api(libs.ktor.serverStatusPages)
    api(libs.ktor.serverCallId)

    api(libs.postgresql)
    api(libs.hikariCp)
    api(libs.exposed.core)
    api(libs.exposed.dao)
    api(libs.exposed.jdbc)
    api(libs.exposed.java.time)
    api(libs.exposed.json)

    api(libs.kotlinx.serialization.json)
}
