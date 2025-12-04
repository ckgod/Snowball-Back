plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.ckgod"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    // Ktor Server (using bundle)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Ktor Client (using bundle)
    implementation(libs.bundles.ktor.client)

    // Logging
    implementation(libs.logback.classic)

    // Database - Exposed (using bundle)
    implementation(libs.bundles.exposed)

    // Database - H2
    implementation(libs.h2.database)

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
