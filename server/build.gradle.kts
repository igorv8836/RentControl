plugins {
    base
}

group = "org.igorv8836.rentcontrol"
version = "1.0.0"

tasks.named("build") {
    dependsOn(subprojects.map { "${it.path}:build" })
}

tasks.named("clean") {
    dependsOn(subprojects.map { "${it.path}:clean" })
}
