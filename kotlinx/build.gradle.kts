import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("minecraft-setup")
    id("publish-setup")
}

version = rootProject.version

impactMod {
    modId.set("impactapi_ktx")
    modName.set("Impact Structures Ktx")
    modGroup.set("space.impact.api.kotlinx")
    mavenGroup.set("space.impact")
    artifactId.set("ImpactStructures-Ktx")
    mavenUrl.set("https://maven.accident.space/repository/maven-releases/")
    usesMixins.set(false)
    coreModClass.set("")
    javaRelease.set(8)
}

// This is a companion library, with no generated mod bootstrap or game registrations.
tasks.matching { it.name.endsWith("BuildConfigClasses") }.configureEach {
    enabled = false
}

dependencies {
    api(project(":"))
    api("space.impact:Forgelin:2.1.3")

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
    doFirst {
        check(classpath.files.none { it.name.startsWith("kotlin-reflect-") }) {
            "KotlinX must run without kotlin-reflect"
        }
    }
}

// The root's legacy ARTIFACT_ID override must not turn this publication into its parent.
afterEvaluate {
    publishing.publications.named<MavenPublication>("maven") {
        artifactId = impactMod.artifactId.get()
    }
}

publishing.repositories.maven {
    name = "consumerTest"
    url = uri(rootProject.layout.buildDirectory.dir("consumer-repository"))
}

tasks.withType<Jar>().configureEach {
    if (name in setOf("jar", "shadowJar", "sourcesJar")) {
        from(listOf(rootProject.file("LICENSE"), rootProject.file("LICENSE.LESSER"), rootProject.file("NOTICE"))) {
            into("META-INF/licenses/impact-structures")
        }
    }
}
