import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.net.URL
import java.nio.file.Files
import java.util.*

plugins {
    id("com.github.johnrengelman.shadow") version ("2.0.4")
}

tasks.withType<ShadowJar> {
    dependsOn("jar")
    archiveName = "${baseName}-${version}-mojangmapping.${extension}"

    // Change the output folder of the plugin.
    // destinationDir = File("C:\\temp\\plugins")

    relocate("kotlin", "com.github.shynixn.petblocks.lib.kotlin")
    relocate("org.intellij", "com.github.shynixn.petblocks.lib.org.intellij")
    relocate("org.jetbrains", "com.github.shynixn.petblocks.lib.org.jetbrains")
    relocate("org.bstats", "com.github.shynixn.petblocks.lib.org.bstats")
    relocate("javax.inject", "com.github.shynixn.petblocks.lib.javax.inject")
    relocate("org.aopalliance", "com.github.shynixn.petblocks.lib.org.aopalliance")
    relocate("org.slf4j", "com.github.shynixn.petblocks.lib.org.slf4j")
    relocate("com.google", "com.github.shynixn.petblocks.lib.com.google")
    relocate ("com.zaxxer", "com.github.shynixn.petblocks.lib.com.zaxxer")
    relocate ("org.apache", "com.github.shynixn.petblocks.lib.org.apache")
    relocate("org.codehaus", "com.github.shynixn.petblocks.lib.org.codehaus")
    relocate("org.checkerframework", "com.github.shynixn.petblocks.lib.org.checkerframework")
    relocate("javax.annotation", "com.github.shynixn.petblocks.lib.javax.annotation")
    relocate("com.fasterxml", "com.github.shynixn.petblocks.lib.com.fasterxml")
    relocate("com.github.shynixn.mccoroutine", "com.github.shynixn.petblocks.lib.com.github.shynixn.mccoroutine")

    exclude("DebugProbesKt.bin")
    exclude("module-info.class")
}

tasks.register("pluginJar", Exec::class.java) {
    // Change the output folder of the plugin.
    //val destinationDir = File("C:/temp/plugins")
    val destinationDir = File(buildDir, "libs")

    dependsOn("shadowJar")
    workingDir = buildDir

    if (!workingDir.exists()) {
        workingDir.mkdir();
    }

    val folder = File(workingDir, "mapping")

    if (!folder.exists()) {
        folder.mkdir()
    }

    val file = File(folder, "SpecialSources.jar")

    if (!file.exists()) {
        URL("https://repo.maven.apache.org/maven2/net/md-5/SpecialSource/1.10.0/SpecialSource-1.10.0-shaded.jar").openStream()
            .use {
                Files.copy(it, file.toPath())
            }
    }

    val shadowJar = tasks.findByName("shadowJar")!! as ShadowJar
    val obfArchiveName = "${shadowJar.baseName}-${shadowJar.version}-obfuscated.${shadowJar.extension}"
    val archiveName = "${shadowJar.baseName}-${shadowJar.version}.${shadowJar.extension}"
    val sourceJarFile = File(buildDir, "libs/" + shadowJar.archiveName)
    val obfJarFile = File(buildDir, "libs/$obfArchiveName")
    val targetJarFile = File(destinationDir, archiveName)

    val obsMapping =
        "java -jar ${file.absolutePath} -i \"$sourceJarFile\" -o \"$obfJarFile\" -m \"\$HOME/.m2/repository/org/spigotmc/minecraft-server/1.17-R0.1-SNAPSHOT/minecraft-server-1.17-R0.1-SNAPSHOT-maps-mojang.txt\" --reverse" +
                "&& java -jar ${file.absolutePath} -i \"$obfJarFile\" -o \"$targetJarFile\" -m \"\$HOME/.m2/repository/org/spigotmc/minecraft-server/1.17-R0.1-SNAPSHOT/minecraft-server-1.17-R0.1-SNAPSHOT-maps-spigot.csrg\""

    if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
        commandLine = listOf("cmd", "/c", obsMapping.replace("\$HOME", "%userprofile%"))
    } else {
        commandLine = listOf("sh", "-c", obsMapping)
    }
}

repositories {
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi")
}

dependencies {
    implementation(project(":petblocks-api"))
    implementation(project(":petblocks-core"))
    implementation(project(":petblocks-bukkit-api"))

    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-108R3"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-109R2"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-110R1"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-111R1"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-112R1"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-113R2"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-114R1"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-115R1"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-116R3"))
    implementation(project(":petblocks-bukkit-plugin:petblocks-bukkit-nms-117R1"))

    implementation("com.github.shynixn.org.bstats:bstats-bukkit:1.7")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:0.0.6")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:0.0.6")

    implementation("org.slf4j:slf4j-jdk14:1.7.30")
    implementation("commons-io:commons-io:2.6")
    implementation("com.google.inject:guice:5.0.1")
    implementation("com.zaxxer:HikariCP:3.4.3")

    implementation("com.google.code.gson:gson:2.8.6")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")

    compileOnly("org.spigotmc:spigot:1.16.4-R0.1-SNAPSHOT")
    compileOnly("com.github.shynixn.headdatabase:hdb-api:1.0")
    compileOnly("me.clip:placeholderapi:2.9.2")

    testCompile("org.xerial:sqlite-jdbc:3.31.1")
    testCompile("ch.vorburger.mariaDB4j:mariaDB4j:2.4.0")
    testCompile("org.spigotmc:spigot:1.8.8-R0.1-SNAPSHOT")
}
