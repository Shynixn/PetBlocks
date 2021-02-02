dependencies {
    implementation(project(":petblocks-api", "default"))

    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
    compileOnly("com.google.inject:guice:4.1.0")
    compileOnly("org.yaml:snakeyaml:1.24")
    compileOnly("org.slf4j:slf4j-api:1.7.25")
    compileOnly("com.zaxxer:HikariCP:3.2.0")

    testCompile("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.3.0")
    testCompile("com.fasterxml.jackson.core:jackson-databind:2.2.3")
    testCompile("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    testCompile("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.3.9")
    testCompile("com.google.inject:guice:4.1.0")
    testCompile("org.yaml:snakeyaml:1.24")
    testCompile("org.slf4j:slf4j-api:1.7.25")
    testCompile("com.zaxxer:HikariCP:3.2.0")

    testCompile("org.xerial:sqlite-jdbc:3.23.1")
    testCompile("ch.vorburger.mariaDB4j:mariaDB4j:2.2.3")
}
