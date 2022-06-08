repositories {
    mavenLocal()
}


dependencies {
    // Dependencies of spigot mojang want to restrict usage to only Java 17. However, we do not care
    // what they want because the general compatibility of this plugin is Java 8. The plugin
    // guarantees that everything works during runtime. This error is a false positive.
    components {
        all {
            allVariants {
                attributes {
                    attribute(Attribute.of("org.gradle.jvm.version", Int::class.javaObjectType), 8)
                }
            }
        }
    }

    implementation(project(":petblocks-api"))
    implementation(project(":petblocks-core"))
    implementation(project(":petblocks-bukkit-api"))

    compileOnly("org.spigotmc:spigot:1.19-R0.1-SNAPSHOT")
    compileOnly("com.google.inject:guice:5.0.1")

    testCompile("org.spigotmc:spigot:1.19-R0.1-SNAPSHOT")
}
