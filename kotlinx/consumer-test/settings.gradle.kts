pluginManagement {
    includeBuild(providers.gradleProperty("buildLogicDir").getOrElse("../../remote-build-logic"))
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://nexus.gtnewhorizons.com/repository/public/")
        maven("https://maven.minecraftforge.net")
    }
}

plugins {
    id("settings-setup")
}

rootProject.name = "ImpactStructures-Ktx-ConsumerTest"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../../remote-build-logic/libs.versions.toml"))
        }
    }
}
