dependencies {
    implementation(project(":petblocks-api", "default"))

    compileOnly("com.google.inject:guice:4.1.0")
    compileOnly("org.yaml:snakeyaml:1.24")
    compileOnly("org.slf4j:slf4j-api:1.7.25")
    compileOnly("com.zaxxer:HikariCP:3.2.0")

    testCompile("org.xerial:sqlite-jdbc:3.23.1")
    testCompile("org.yaml:snakeyaml:1.24")
    testCompile("ch.vorburger.mariaDB4j:mariaDB4j:2.2.3")
}
