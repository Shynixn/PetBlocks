import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*
import java.io.*

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.6.10")
    id("com.github.johnrengelman.shadow") version ("7.0.0")
}

group = "com.github.shynixn"
version = "9.4.0"

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
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.1")

    // Library dependencies with legacy compatibility, we can use more up-to-date version in the plugin.yml
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.13.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.13.0")
    implementation("com.google.inject:guice:5.0.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.3")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.openjdk.nashorn:nashorn-core:15.4")

    // Custom dependencies
    implementation("com.github.shynixn.mcutils:common:1.0.65")
    implementation("com.github.shynixn.mcutils:packet:1.0.86")
    implementation("com.github.shynixn.mcutils:database:1.0.21")
    implementation("com.github.shynixn.mcutils:pathfinder:1.0.19")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    testImplementation("org.mockito:mockito-core:2.23.0")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:2.4.0")
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
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
 * Create all plugin jar files.
 */
tasks.register("pluginJars") {
    dependsOn("pluginJarLatest")
    dependsOn("pluginJarPremium")
    dependsOn("pluginJarLegacy")
}

/**
 * Create legacy plugin jar file.
 */
tasks.register("relocatePluginJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("shadowJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("shadowJar") as Jar).archiveName)))
    archiveName = "${baseName}-${version}-relocate.${extension}"
    relocate("com.github.shynixn.mcutils", "com.github.shynixn.petblocks.lib.com.github.shynixn.mcutils")
}

/**
 * Create latest plugin jar file.
 */
tasks.register("pluginJarLatest", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("relocatePluginJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("relocatePluginJar") as Jar).archiveName)))
    archiveName = "${baseName}-${version}-latest.${extension}"
    // destinationDir = File("C:\\temp\\plugins")

    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_8_R3/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_9_R2/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_18_R1/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_18_R2/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_19_R1/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_19_R2/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_19_R3/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_20_R1/**")
    exclude("com/github/shynixn/mcutils/**")
    exclude("com/github/shynixn/mccoroutine/**")
    exclude("kotlin/**")
    exclude("org/**")
    exclude("kotlinx/**")
    exclude("javax/**")
    exclude("com/google/**")
    exclude("com/fasterxml/**")
    exclude("com/zaxxer/**")
}

/**
 * Create premium plugin jar file.
 */
tasks.register("pluginJarPremium", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("relocatePluginJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("relocatePluginJar") as Jar).archiveName)))
    archiveName = "${baseName}-${version}-premium.${extension}"
    // destinationDir = File("C:\\temp\\plugins")

    exclude("com/github/shynixn/mcutils/**")
    exclude("com/github/shynixn/mccoroutine/**")
    exclude("kotlin/**")
    exclude("org/**")
    exclude("kotlinx/**")
    exclude("javax/**")
    exclude("com/zaxxer/**")
    exclude("com/google/**")
    exclude("com/fasterxml/**")
}

/**
 * Create legacy plugin jar file.
 */
tasks.register("relocateLegacyPluginJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("shadowJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("shadowJar") as Jar).archiveName)))
    archiveName = "${baseName}-${version}-legacy-relocate.${extension}"
    relocate("com.github.shynixn.mcutils", "com.github.shynixn.petblocks.lib.com.github.shynixn.mcutils")
    relocate("kotlin", "com.github.shynixn.petblocks.lib.kotlin")
    relocate("org.intellij", "com.github.shynixn.petblocks.lib.org.intelli")
    relocate("org.aopalliance", "com.github.shynixn.petblocks.lib.org.aopalliance")
    relocate("org.checkerframework", "com.github.shynixn.petblocks.lib.org.checkerframework")
    relocate("org.jetbrains", "com.github.shynixn.petblocks.lib.org.jetbrains")
    relocate("org.openjdk.nashorn", "com.github.shynixn.petblocks.lib.org.openjdk.nashorn")
    relocate("org.slf4j", "com.github.shynixn.petblocks.lib.org.slf4j")
    relocate("org.objectweb", "com.github.shynixn.petblocks.lib.org.objectweb")
    relocate("javax.annotation", "com.github.shynixn.petblocks.lib.javax.annotation")
    relocate("javax.inject", "com.github.shynixn.petblocks.lib.javax.inject")
    relocate("kotlinx.coroutines", "com.github.shynixn.petblocks.lib.kotlinx.coroutines")
    relocate("com.google", "com.github.shynixn.petblocks.lib.com.google")
    relocate("com.fasterxml", "com.github.shynixn.petblocks.lib.com.fasterxml")
    relocate("com.zaxxer", "com.github.shynixn.petblocks.lib.com.zaxxer")
    relocate("com.github.shynixn.mccoroutine", "com.github.shynixn.petblocks.lib.com.github.shynixn.mccoroutine")
    exclude("plugin.yml")
    rename("plugin-legacy.yml", "plugin.yml")
}

/**
 * Create legacy plugin jar file.
 */
tasks.register("pluginJarLegacy", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("relocateLegacyPluginJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("relocateLegacyPluginJar") as Jar).archiveName)))
    archiveName = "${baseName}-${version}-legacy.${extension}"
    // destinationDir = File("C:\\temp\\plugins")
    exclude("com/github/shynixn/mcutils/**")
    exclude("org/**")
    exclude("kotlin/**")
    exclude("kotlinx/**")
    exclude("javax/**")
    exclude("com/google/**")
    exclude("com/github/shynixn/mccoroutine/**")
    exclude("com/fasterxml/**")
    exclude("com/zaxxer/**")
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
