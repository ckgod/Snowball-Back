plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":common"))
    implementation(project(":domain"))

    // Database - Exposed
    implementation(libs.bundles.exposed)
    implementation(libs.h2.database)

    // Ktor Client (외부 API 호출)
    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.kotlinx.json)
}
