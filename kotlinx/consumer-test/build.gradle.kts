plugins {
    id("minecraft-setup")
}

impactMod {
    modId.set("impactapi_kotlinx_consumer")
    modGroup.set("space.impact.api.kotlinx.consumer")
    mavenGroup.set("space.impact.test")
    usesMixins.set(false)
    coreModClass.set("")
    javaRelease.set(8)
}

tasks.matching { it.name.endsWith("BuildConfigClasses") }.configureEach {
    enabled = false
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "publishedLibraries"
                url = uri(providers.gradleProperty("consumerRepository")
                    .getOrElse("../../build/consumer-repository"))
            }
        }
        filter {
            includeModule("space.impact", "ImpactStructures")
            includeModule("space.impact", "ImpactStructures-Ktx")
        }
    }
}

dependencies {
    implementation("space.impact:ImpactStructures-Ktx:${providers.gradleProperty("impactVersion").get()}")
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209")
}

tasks.test {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
}

val verifyPublishedDependencies by tasks.registering {
    dependsOn(tasks.named("reobfJar"))
    doLast {
        val development = configurations.getByName("compileClasspath").resolvedConfiguration.resolvedArtifacts
        val production = configurations.getByName("reobfJarConfiguration").resolvedConfiguration.resolvedArtifacts
        val expected = setOf("ImpactStructures", "ImpactStructures-Ktx")
        val devLibraries = development.filter { it.moduleVersion.id.name in expected }
        val productionLibraries = production.filter { it.moduleVersion.id.name in expected }
        check(devLibraries.map { it.moduleVersion.id.name }.toSet() == expected)
        check(devLibraries.all { it.file.name.endsWith("-dev.jar") })
        check(productionLibraries.map { it.moduleVersion.id.name }.toSet() == expected)
        check(productionLibraries.all { !it.file.name.endsWith("-dev.jar") })
        check(configurations.getByName("testRuntimeClasspath").files.none {
            it.name.startsWith("kotlin-reflect-")
        })
    }
}

tasks.named("check") {
    dependsOn(verifyPublishedDependencies)
}
