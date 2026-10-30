pluginManagement {
    includeBuild("remote-build-logic")
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://nexus.gtnewhorizons.com/repository/public/") {
            content { includeGroupByRegex("com\\.gtnewhorizons(\\..*)?") }
        }
        maven("https://maven.minecraftforge.net")
    }
}

plugins {
    id("settings-setup")
}

rootProject.name = "ImpactStructures"

include(":ImpactStructures-Ktx")
project(":ImpactStructures-Ktx").projectDir = file("kotlinx")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("remote-build-logic/libs.versions.toml"))
        }
    }
}
