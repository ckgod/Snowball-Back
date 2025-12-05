plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    // 모든 모듈 통합
    implementation(project(":common"))
    implementation(project(":domain"))
    implementation(project(":infrastructure"))

    // Ktor Server (presentation 계층 포함)
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Ktor Client (Application에서 생성)
    implementation(libs.bundles.ktor.client)

    // Logging
    implementation(libs.logback.classic)

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
}
