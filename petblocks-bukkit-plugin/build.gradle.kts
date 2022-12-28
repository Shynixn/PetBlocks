import java.util.*
import java.io.*

plugins {
    id("com.github.johnrengelman.shadow") version ("6.1.0")
}

dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.9.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("com.google.inject:guice:5.0.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")
    runtimeOnly("com.zaxxer:HikariCP:4.0.3")

    implementation("com.github.shynixn.mcutils:common:1.0.19")
    implementation("com.github.shynixn.mcutils:packet:1.0.29")
    implementation("com.github.shynixn.mcutils:database:1.0.5")
    implementation("com.github.shynixn.mcutils:pathfinder:1.0.14")
    implementation("com.github.shynixn.mcutils:physic:1.0.17")

    implementation(project(":petblocks-bukkit-api"))
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependsOn("jar")

    destinationDir = File("C:\\temp\\plugins")

    //  TODO  relocate("com.github.shynixn.mcutils","com.github.shynixn.mctennis.mcutils")
}

tasks.register("languageFile", Exec::class.java) {
    val kotlinSrcFolder = project.sourceSets.toList()[0].allJava.srcDirs.first { e -> e.endsWith("java") }
    val languageKotlinFile =
        kotlinSrcFolder.resolve("com/github/shynixn/petblocks/bukkit/PetBlocksLanguage.kt")
    val resourceFile = kotlinSrcFolder.parentFile.resolve("resources").resolve("lang").resolve("en_us.properties")
    val bundle = FileInputStream(resourceFile).use { stream ->
        PropertyResourceBundle(stream)
    }

    val contents = ArrayList<String>()
    contents.add("package com.github.shynixn.petblocks.bukkit")
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

    commandLine = arrayListOf("cmd", "/c")
}
