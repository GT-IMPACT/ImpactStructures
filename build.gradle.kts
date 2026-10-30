plugins {
    id("minecraft-setup")
    id("publish-setup")
}

impactMod {
    modId.set("impactapi")
    modName.set("Impact Structures")
    modGroup.set("space.impact")
    mavenGroup.set("space.impact")
    mavenUrl.set("https://maven.accident.space/repository/maven-releases/")
    usesMixins.set(true)
    coreModClass.set("api.mixinplugin.ImpactEarlyMixins")
    mixinConfigs.set(listOf("mixins.impactapi.json"))
    accessTransformers.set(listOf("impactapi_at.cfg"))
}

val modId = impactMod.modId.get()
val modName = impactMod.modName.get()
val modGroup = impactMod.modGroup.get()

buildConfig {
    packageName("space.impact.$modId")
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

tasks.withType<Jar>().configureEach {
    if (name in setOf("jar", "shadowJar", "sourcesJar")) {
        from(listOf("LICENSE", "LICENSE.LESSER", "NOTICE")) {
            into("META-INF/licenses/impact-structures")
        }
    }
}
