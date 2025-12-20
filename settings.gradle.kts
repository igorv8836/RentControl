rootProject.name = "RentControl"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(
    ":composeApp",
    ":server",
    ":shared",
    ":libraries:bdui",
    ":libraries:bdui:contract",
    ":libraries:bdui:runtime",
    ":libraries:bdui:components",
    ":libraries:bdui:renderer",
    ":libraries:bdui:actions",
    ":libraries:bdui:platform-android",
    ":libraries:bdui:platform-ios",
    ":libraries:bdui:testing",
    ":libraries:bdui:tooling",
    ":libraries:bdui:network",
    ":libraries:bdui:demo",
)
