import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.6.21"
}

group = "com.github.shynixn.petblocks"
version = "1.0-SNAPSHOT"

allprojects{
    apply(plugin = "kotlin")


    repositories {
        mavenCentral()
        maven("https://shynixn.github.io/m2/repository/mcutils")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }

    dependencies {
        // Third Party
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
        compileOnly("org.spigotmc:spigot-api:1.16.3-R0.1-SNAPSHOT")
        compileOnly("com.github.shynixn.mcutils:common:1.0.19")
        compileOnly("com.github.shynixn.mcutils:database:1.0.3")
        compileOnly("com.fasterxml.jackson.core:jackson-databind:2.14.1")

        // Test
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}

tasks.register("printVersion") {
    println(this.project.version)
}
