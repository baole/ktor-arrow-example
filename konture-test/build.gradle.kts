plugins {
    kotlin("jvm")
    id("io.github.baole.konture")
}

dependencies {
    // Compile and scan the main application module
    implementation(project(":"))

    // Standard test implementation of the unified Konture assertion library
    testImplementation(libs.konture.test)

    // JUnit 5 test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
