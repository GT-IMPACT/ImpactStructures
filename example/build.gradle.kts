plugins {
    id("minecraft-setup")
}

version = rootProject.version

impactMod {
    modId.set("impactstructures_example")
    modName.set("Impact Structures Animation Example")
    modGroup.set("space.impact.structures.example")
    mavenGroup.set("space.impact")
    artifactId.set("ImpactStructures-Example")
    usesMixins.set(false)
    coreModClass.set("")
}

tasks.matching { it.name.endsWith("BuildConfigClasses") }.configureEach {
    enabled = false
}

dependencies {
    implementation(project(":"))
    testImplementation(platform("org.junit:junit-bom:5.9.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(8))
    })
}

// Keep the demo out of the library's runtime and publications. Run this project's client explicitly.
val angelicaRuntime by configurations.creating {
    isCanBeConsumed = false
}
dependencies {
    angelicaRuntime("com.github.GTNewHorizons:Angelica:2.2.43:dev") {
        isTransitive = false
    }
    angelicaRuntime("com.github.GTNewHorizons:GTNHLib:0.11.52:dev")
}
tasks.named<JavaExec>("runClient") {
    if (providers.gradleProperty("angelica").map(String::toBoolean).orElse(true).get()) {
        classpath += angelicaRuntime
    }
}
