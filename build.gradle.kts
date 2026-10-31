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

tasks.named("check") {
    dependsOn(":ImpactStructures-Ktx:check")
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
