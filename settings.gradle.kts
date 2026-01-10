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
    ":server:bdui-server",
    ":server:rent-control-server",
    ":server:rent-control-server:app",
    ":server:rent-control-server:foundation",
    ":server:rent-control-server:integrations",
    ":server:rent-control-server:modules:auth",
    ":server:rent-control-server:modules:me",
    ":server:rent-control-server:modules:users",
    ":shared",
    ":libraries:bdui",
    ":libraries:bdui:contract",
    ":libraries:bdui:runtime",
    ":libraries:bdui:components",
    ":libraries:bdui:renderer",
    ":libraries:bdui:actions",
    ":libraries:bdui:testing",
    ":libraries:bdui:core",
    ":libraries:bdui-backend:contract",
    ":libraries:bdui-backend:core",
    ":libraries:bdui-backend:dsl",
    ":libraries:bdui-backend:mapper",
    ":libraries:bdui-backend:renderer",
    ":libraries:bdui-backend:runtime",
    ":libraries:bdui:tooling",
    ":libraries:bdui:cache",
    ":libraries:bdui:network",
    ":libraries:bdui:demo",
    ":libraries:bdui:navigation",
    ":libraries:bdui:engine",
    ":libraries:bdui:logger",
)
