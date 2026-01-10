import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
    base
}

group = "org.igorv8836.rentcontrol"
version = "1.0.0"

subprojects {
    group = rootProject.group
    version = rootProject.version

    plugins.withId("org.jetbrains.kotlin.jvm") {
        the<KotlinJvmProjectExtension>().jvmToolchain(17)
    }
}

tasks.register("test") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("test") })
}

tasks.named("check") {
    dependsOn(subprojects.mapNotNull { it.tasks.findByName("check") })
}
