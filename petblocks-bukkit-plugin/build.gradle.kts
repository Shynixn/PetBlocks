dependencies {
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.9.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("com.google.inject:guice:5.0.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.14.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.1")

    implementation("com.github.shynixn.mcutils:common:1.0.19")
    implementation("com.github.shynixn.mcutils:packet:1.0.29")
    implementation("com.github.shynixn.mcutils:database:1.0.3")
    implementation("com.github.shynixn.mcutils:pathfinder:1.0.14")
    implementation("com.github.shynixn.mcutils:physic:1.0.17")

    implementation(project(":petblocks-bukkit-api"))
}
