import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.*
import java.util.*

plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.9.25")
    id("com.github.johnrengelman.shadow") version ("7.0.0")
}

group = "com.github.shynixn"
version = "9.21.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://shynixn.github.io/m2/repository/releases")
    maven(System.getenv("SHYNIXN_MCUTILS_REPOSITORY_2025")) // All MCUTILS libraries are private and not OpenSource.
}

dependencies {
    // Compile Only
    compileOnly("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.1")

    // Library dependencies with legacy compatibility, we can use more up-to-date version in the plugin.yml
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.21.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.21.0")
    runtimeOnly("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.21.0")
    runtimeOnly("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.21.0")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("com.zaxxer:HikariCP:4.0.3")
    implementation("org.openjdk.nashorn:nashorn-core:15.4")

    // Custom dependencies
    implementation("com.github.shynixn.shygui:shygui:1.2.0")
    implementation("com.github.shynixn.mcutils:common:2025.4")
    implementation("com.github.shynixn.mcutils:packet:2025.3")
    implementation("com.github.shynixn.mcutils:database:2025.2")
    implementation("com.github.shynixn.mcutils:pathfinder:2025.1")
    implementation("com.github.shynixn.mcutils:javascript:2025.1")

    // Test
    testImplementation(kotlin("test"))
    testImplementation("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    testImplementation("org.mockito:mockito-core:2.23.0")
    testImplementation("ch.vorburger.mariaDB4j:mariaDB4j:2.4.0")
    testImplementation("com.mysql:mysql-connector-j:8.3.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.test {
    useJUnitPlatform()
}

/**
 * Include all but exclude debugging classes.
 */
tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    dependsOn("jar")
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-shadowjar.${archiveExtension.get()}")
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
 * Relocate Plugin Jar.
 */
tasks.register("relocatePluginJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("shadowJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("shadowJar") as Jar).archiveFileName.get())))
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-relocate.${archiveExtension.get()}")
    relocate("com.github.shynixn.mcutils", "com.github.shynixn.petblocks.lib.com.github.shynixn.mcutils")
    relocate("com.fasterxml", "com.github.shynixn.petblocks.lib.com.fasterxml")
    relocate("com.github.shynixn.shygui", "com.github.shynixn.petblocks.lib.com.github.shynixn.shygui")
}

/**
 * Create latest plugin jar file.
 */
tasks.register("pluginJarLatest", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("relocatePluginJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("relocatePluginJar") as Jar).archiveFileName.get())))
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-latest.${archiveExtension.get()}")
    // destinationDirectory.set(File("C:\\temp\\plugins"))

    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_8_R3/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_9_R2/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_17_R1/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_18_R1/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_18_R2/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_19_R1/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_19_R2/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_19_R3/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_20_R1/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_20_R2/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_20_R3/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_20_R4/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_21_R1/**")
    exclude("com/github/shynixn/petblocks/lib/com/github/shynixn/mcutils/packet/nms/v1_21_R2/**")
    exclude("com/github/shynixn/mcutils/**")
    exclude("com/github/shynixn/shygui/**")
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
    from(zipTree(File("./build/libs/" + (tasks.getByName("relocatePluginJar") as Jar).archiveFileName.get())))
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-premium.${archiveExtension.get()}")
    // destinationDirectory.set(File("C:\\temp\\plugins"))

    exclude("com/github/shynixn/mcutils/**")
    exclude("com/github/shynixn/shygui/**")
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
 * Relocate legacy plugin jar file.
 */
tasks.register("relocateLegacyPluginJar", com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class.java) {
    dependsOn("shadowJar")
    from(zipTree(File("./build/libs/" + (tasks.getByName("shadowJar") as Jar).archiveFileName.get())))
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-legacy-relocate.${archiveExtension.get()}")
    relocate("com.github.shynixn.mcutils", "com.github.shynixn.petblocks.lib.com.github.shynixn.mcutils")
    relocate("com.github.shynixn.shygui", "com.github.shynixn.petblocks.lib.com.github.shynixn.shygui")
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
    from(zipTree(File("./build/libs/" + (tasks.getByName("relocateLegacyPluginJar") as Jar).archiveFileName.get())))
    archiveFileName.set("${archiveBaseName.get()}-${archiveVersion.get()}-legacy.${archiveExtension.get()}")
    // destinationDirectory.set(File("C:\\temp\\plugins"))
    exclude("com/github/shynixn/mcutils/**")
    exclude("com/github/shynixn/shygui/**")
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
    val contractFile = kotlinSrcFolder.resolve("com/github/shynixn/petblocks/contract/PetBlocksLanguage.kt")
    val resourceFile = kotlinSrcFolder.parentFile.resolve("resources").resolve("lang").resolve("en_us.yml")
    val lines = resourceFile.readLines()

    val contractContents = ArrayList<String>()
    val ignoredKeys = listOf(
        "playerNotFoundMessage",
        "reloadMessage",
        "noPermissionCommand",
        "commandUsage",
        "commandDescription",
        "commandSenderHasToBePlayer",
        "manipulateOtherMessage",
        "reloadCommandHint",
        "closeCommandHint",
        "backCommandHint",
        "openCommandHint",
        "nextCommandHint",
        "messageCommandHint",
        "guiMenuNotFoundMessage",
        "guiMenuNoPermissionMessage",
        "cannotParseItemStackError",
        "rowColOutOfRangeError"
    )
    contractContents.add("package com.github.shynixn.petblocks.contract")
    contractContents.add("")
    contractContents.add("import com.github.shynixn.mcutils.common.language.LanguageItem")
    contractContents.add("import com.github.shynixn.mcutils.common.language.LanguageProvider")
    contractContents.add("import com.github.shynixn.shygui.contract.ShyGUILanguage")
    contractContents.add("")
    contractContents.add("interface PetBlocksLanguage : LanguageProvider, ShyGUILanguage {")
    for (key in lines) {
        if (key.toCharArray()[0].isLetter()) {
            if (ignoredKeys.contains(key.substring(0, key.length - 1))) {
                continue
            }

            contractContents.add("  var ${key} LanguageItem")
            contractContents.add("")
        }
    }
    contractContents.removeLast()
    contractContents.add("}")

    contractFile.printWriter().use { out ->
        for (line in contractContents) {
            out.println(line)
        }
    }

    val implFile = kotlinSrcFolder.resolve("com/github/shynixn/petblocks/PetBlocksLanguageImpl.kt")
    val implContents = ArrayList<String>()
    implContents.add("package com.github.shynixn.petblocks")
    implContents.add("")
    implContents.add("import com.github.shynixn.mcutils.common.language.LanguageItem")
    implContents.add("import com.github.shynixn.petblocks.contract.PetBlocksLanguage")
    implContents.add("")
    implContents.add("class PetBlocksLanguageImpl : PetBlocksLanguage {")
    implContents.add(
        " override val names: List<String>\n" +
                "  get() = listOf(\"en_us\",\"es_es\")"
    )

    for (i in 0 until lines.size) {
        val key = lines[i]

        if (key.toCharArray()[0].isLetter()) {
            var text = ""
            var j = i
            while (true) {
                if (lines[j].contains("text:")) {
                    text = lines[j]
                    break
                }
                j++
            }

            implContents.add(" override var ${key.replace(":", "")} = LanguageItem(${text.replace("  text: ", "")})")
            implContents.add("")
        }
    }
    implContents.removeLast()
    implContents.add("}")

    implFile.printWriter().use { out ->
        for (line in implContents) {
            out.println(line)
        }
    }
}

tasks.register("printVersion") {
    println(version)
}
