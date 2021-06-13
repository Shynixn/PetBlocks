dependencies {
    implementation(project(":petblocks-api"))
    implementation(project(":petblocks-core"))
    implementation(project(":petblocks-bukkit-api"))

    compileOnly("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly("com.google.inject:guice:5.0.1")

    testCompile("org.spigotmc:spigot:1.17-R0.1-SNAPSHOT:remapped-mojang")
}