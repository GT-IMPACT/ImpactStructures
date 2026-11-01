import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("minecraft-setup")
    id("publish-setup")
}

impactMod {
    modId.set("impactapi")
    modName.set("Impact Structures")
    modGroup.set("space.impact.structures")
    mavenGroup.set("space.impact")
    artifactId.set("ImpactStructures")
    mavenUrl.set("https://maven.accident.space/repository/maven-releases/")
    usesMixins.set(true)
    coreModClass.set("mixinplugin.ImpactEarlyMixins")
    mixinConfigs.set(listOf("mixins.impactapi.json"))
    accessTransformers.set(listOf("impactapi_at.cfg"))
}

val modId = impactMod.modId.get()
val modName = impactMod.modName.get()
val modGroup = impactMod.modGroup.get()

// Client renderer runtime: never add it to the server runtime or published metadata.
val angelicaRuntime by configurations.creating {
    isCanBeConsumed = false
}
val angelicaVersion = "2.2.43"
val withAngelica = providers.gradleProperty("angelica").map(String::toBoolean).orElse(true)

tasks.named<JavaExec>("runClient") {
    if (withAngelica.get()) {
        classpath += angelicaRuntime
    }
}

buildConfig {
    packageName(modGroup)
    className("BuildConfigKt")
    useJavaOutput()
    buildConfigField("String", "MODID", "\"${modId}\"")
    buildConfigField("String", "MODNAME", "\"${modName}\"")
    buildConfigField("String", "VERSION", "\"${project.version}\"")
    buildConfigField("String", "GROUPNAME", "\"${modGroup}\"")
}

dependencies {
    compileOnly("com.github.GTNewHorizons:Angelica:$angelicaVersion:dev") {
        isTransitive = false
    }
    angelicaRuntime("com.github.GTNewHorizons:Angelica:$angelicaVersion:dev") {
        isTransitive = false
    }
    angelicaRuntime("com.github.GTNewHorizons:GTNHLib:0.11.52:dev")
    compileOnly("com.github.GTNewHorizons:Applied-Energistics-2-Unofficial:rv3-beta-1086-GTNH:dev") { isTransitive = false }
    compileOnly("com.github.GTNewHorizons:WirelessCraftingTerminal:1.12.16:dev") { isTransitive = false }
    compileOnly("com.github.GTNewHorizons:AE2FluidCraft-Rework:1.5.114-gtnh:dev") { isTransitive = false }
    compileOnly("com.github.GTNewHorizons:Baubles-Expanded:2.2.25-GTNH:dev") { isTransitive = false }
    compileOnly("curse.maven:cofh-core-69162:2388751") { isTransitive = false }
    compileOnly(modUtils.deobfuscate(files("libs/forestry-4.2.29-impact.jar")))
    compileOnly(modUtils.deobfuscate(files("libs/backpack-2.2.4-1.7.x.jar")))
    compileOnly(modUtils.deobfuscate(files("libs/extrautilities-1.2.12.jar")))

    // Types referenced by AE2's API; not runtime dependencies of Impact Structures.
    compileOnly("com.github.GTNewHorizons:GTNHLib:0.11.52:dev") { isTransitive = false }
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnly("it.unimi.dsi:fastutil:8.5.18") { isTransitive = false }
    val nei = "com.github.GTNewHorizons:NotEnoughItems:2.8.158-GTNH:dev"
    compileOnly(nei)
    runtimeOnlyNonPublishable(nei)

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

// Keep ordinary tests on the no-integration runtime; exercise real mod APIs separately.
val compatTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output + sourceSets.test.get().compileClasspath +
        configurations.compileClasspath.get()
    runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().runtimeClasspath + compileClasspath
}
configurations[compatTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
dependencies {
    add(compatTest.implementationConfigurationName, "org.mockito:mockito-core:4.11.0")
    add(compatTest.runtimeOnlyConfigurationName, "net.industrial-craft:industrialcraft-2:2.2.828-experimental:dev") {
        isTransitive = false
    }
}
val testCompat by tasks.registering(Test::class) {
    description = "Tests optional integrations against the selected modpack APIs."
    group = "verification"
    testClassesDirs = compatTest.output.classesDirs
    classpath = compatTest.runtimeClasspath
    useJUnitPlatform()
    javaLauncher.set(javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(8)) })
}

tasks.named("check") {
    dependsOn(":ImpactStructures-Ktx:check")
    dependsOn(testCompat)
}

tasks.named("build") {
    dependsOn(":ImpactStructures-Ktx:build")
}

// Publication coordinates must match project names for local reobfuscation.
afterEvaluate {
    publishing.publications.named<MavenPublication>("maven") {
        artifactId = impactMod.artifactId.get()
    }
}

publishing.repositories.maven {
    name = "consumerTest"
    url = uri(layout.buildDirectory.dir("consumer-repository"))
}

val prepareKotlinXConsumerBuildLogic by tasks.registering(Copy::class) {
    from("remote-build-logic") {
        include("settings.gradle.kts", "libs.versions.toml", "plugins/build.gradle.kts", "plugins/src/main/**")
    }
    into(layout.buildDirectory.dir("consumer-build-logic"))
}

tasks.register<Exec>("verifyKotlinXConsumer") {
    group = "verification"
    description = "Tests the independently published KotlinX dev and reobf variants."
    dependsOn(
        ":publishMavenPublicationToConsumerTestRepository",
        ":ImpactStructures-Ktx:publishMavenPublicationToConsumerTestRepository",
        prepareKotlinXConsumerBuildLogic,
    )
    doFirst {
        val javaBinary = if (System.getProperty("os.name").startsWith("Windows")) "java.exe" else "java"
        commandLine(
            file("${System.getProperty("java.home")}/bin/$javaBinary"),
            "-jar", file("gradle/wrapper/gradle-wrapper.jar"),
            "-p", file("kotlinx/consumer-test"),
            "build", "--no-daemon", "--console=plain",
            "-PimpactVersion=${project.version}",
            "-PconsumerRepository=${layout.buildDirectory.dir("consumer-repository").get().asFile.absolutePath}",
            "-PbuildLogicDir=${layout.buildDirectory.dir("consumer-build-logic").get().asFile.absolutePath}",
        )
    }
}

tasks.withType<Jar>().configureEach {
    if (name in setOf("jar", "shadowJar", "sourcesJar")) {
        from(listOf("LICENSE", "LICENSE.LESSER", "NOTICE")) {
            into("META-INF/licenses/impact-structures")
        }
    }
}
