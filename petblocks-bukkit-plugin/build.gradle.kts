import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URL
import java.nio.file.Files
import java.util.*

plugins {
    id("com.github.johnrengelman.shadow") version ("5.2.0")
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    classifier = "plugin"
    archiveName = "${baseName}-${version}.${extension}"

    // Change the output folder of the plugin.
    // destinationDir = File("C:\\temp\\plugins")
    exclude("DebugProbesKt.bin")
    exclude("module-info.class")
}

repositories {
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi")
    maven("https://shynixn.github.io/m2/repository/mcutils")
}

dependencies {
    // Compile Only
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.9.2")

    // Plugin.yml Shade dependencies
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.7.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.7.0")
    implementation("com.google.inject:guice:5.0.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.2")
    implementation("com.google.code.gson:gson:2.8.6")

    // Custom dependencies
    implementation("com.github.shynixn.mcutils:common:1.0.19")
    implementation("com.github.shynixn.mcutils:packet:1.0.29")
    implementation("com.github.shynixn.mcutils:database:1.0.3")
    implementation("com.github.shynixn.mcutils:physic:1.0.17")

    testImplementation(kotlin("test"))
    testImplementation("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    testImplementation("org.mockito:mockito-core:2.23.0")
}
