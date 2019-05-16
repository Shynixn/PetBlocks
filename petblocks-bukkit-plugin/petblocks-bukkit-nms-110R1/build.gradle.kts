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
    implementation(project(":petblocks-core"))
    implementation(project(":petblocks-bukkit-api"))

    compileOnly("org.spigotmc:spigot110R1:1.10.2-R1.0")
    compileOnly("com.google.inject:guice:4.1.0")

    testCompile("org.jetbrains.kotlin:kotlin-test")
    testCompile("org.jetbrains.kotlin:kotlin-test-junit")
    testCompile("org.mockito:mockito-core:2.23.0")
    testCompile("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testCompile("org.spigotmc:spigot110R1:1.10.2-R1.0")
    
    testRuntime("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}