import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*
import java.io.*

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.6.10")
    id("com.github.johnrengelman.shadow") version ("7.0.0")
}

group = "com.github.shynixn"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi")
    maven("https://shynixn.github.io/m2/repository/mcutils")
    maven("https://repo.opencollab.dev/main/")
}

tasks.register("printVersion") {
    println(version)
}

dependencies {
    // Compile Only
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.9.2")

    // Plugin.yml Shade dependencies
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.13.0")
    compileOnly("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.13.0")
    compileOnly("com.google.inject:guice:5.0.1")
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    compileOnly("com.google.code.gson:gson:2.8.6")
    compileOnly("org.openjdk.nashorn:nashorn-core:15.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.3")

    // Custom dependencies
    implementation("com.jayway.jsonpath:json-path:2.8.0")
    implementation("com.github.shynixn.mcutils:common:1.0.37")
    implementation("com.github.shynixn.mcutils:packet:1.0.55")
    implementation("com.github.shynixn.mcutils:database:1.0.13")
    implementation("com.github.shynixn.mcutils:pathfinder:1.0.19")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    testImplementation("org.mockito:mockito-core:2.23.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging.showStandardStreams = true
    failFast = true

    testLogging {
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED
        )
        displayGranularity = 0
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

/**
 * Include all but exclude debugging classes.
 */
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependsOn("jar")
    archiveName = "${baseName}-${version}-shadowjar.${extension}"
    exclude("DebugProbesKt.bin")
    exclude("module-info.class")
}

/**
 * Create latest plugin jar file.
 */
tasks.register("pluginJarLatest", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("shadowJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("shadowJar") as Jar).archiveName)))
    archiveName = "${baseName}-${version}-latest.${extension}"
    destinationDir = File("C:\\temp\\plugins")
    exclude("kotlin/**")
    exclude("org/**")
    exclude("kotlinx/**")
    exclude("javax/**")
    exclude("com/google/**")
    exclude("com/fasterxml/**")
    exclude("plugin-legacy.yml")
}


tasks.register("languageFile") {
    val kotlinSrcFolder = project.sourceSets.toList()[0].allJava.srcDirs.first { e -> e.endsWith("java") }
    val languageKotlinFile = kotlinSrcFolder.resolve("com/github/shynixn/petblocks/PetBlocksLanguage.kt")
    val resourceFile = kotlinSrcFolder.parentFile.resolve("resources").resolve("lang").resolve("en_us.properties")
    val bundle = FileInputStream(resourceFile).use { stream ->
        PropertyResourceBundle(stream)
    }

    val contents = ArrayList<String>()
    contents.add("package com.github.shynixn.petblocks")
    contents.add("")
    contents.add("object PetBlocksLanguage {")
    for (key in bundle.keys) {
        val value = bundle.getString(key)
        contents.add("  /** $value **/")
        contents.add("  var ${key} : String = \"$value\"")
        contents.add("")
    }
    contents.removeLast()
    contents.add("}")

    languageKotlinFile.printWriter().use { out ->
        for (line in contents) {
            out.println(line)
        }
    }
}
