plugins {
    alias(libs.plugins.jvm)
    application
    kotlin("plugin.serialization") version "2.0.0-RC1"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    implementation(libs.guava)
    implementation("io.ktor:ktor-client-core:3.0.0-rc-1")
    implementation("io.ktor:ktor-client-cio:3.0.0-rc-1")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.0-rc-1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0-rc-1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.2")
    implementation("org.slf4j:slf4j-api:2.0.7")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("io.ktor:ktor-client-logging:3.0.0-rc-1")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.5")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
    testImplementation("io.mockk:mockk:1.13.3")


}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(19)
    }
}

application {
    mainClass = "org.example.NewsApiClientKt"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
