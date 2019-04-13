import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

tasks {
    test {
        testLogging {
            events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.STARTED)
            displayGranularity = 0
            showExceptions = true
            showCauses = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

dependencies {
    implementation(project(":petblocks-api"))

    compileOnly("com.google.inject:guice:4.1.0")
    compileOnly("org.yaml:snakeyaml:1.24")
    compileOnly("org.slf4j:slf4j-api:1.7.25")
    compileOnly("com.zaxxer:HikariCP:3.2.0")

    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
    testCompile("org.mockito:mockito-core:2.23.0")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testCompile("org.xerial:sqlite-jdbc:3.23.1")
    testCompile("org.yaml:snakeyaml:1.24")
    testCompile("ch.vorburger.mariaDB4j:mariaDB4j:2.2.3")

    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}