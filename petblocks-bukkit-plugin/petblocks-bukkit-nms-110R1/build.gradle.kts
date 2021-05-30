dependencies {
    implementation(project(":petblocks-api"))
    implementation(project(":petblocks-core"))
    implementation(project(":petblocks-bukkit-api"))

    compileOnly("org.spigotmc:spigot110R1:1.10.2-R1.0")
    compileOnly("com.google.inject:guice:5.0.1")

    testCompile("org.spigotmc:spigot110R1:1.10.2-R1.0")
}
