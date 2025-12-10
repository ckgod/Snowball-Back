plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":infrastructure:database"))

    implementation(libs.bundles.ktor.client)
    implementation(libs.ktor.serialization.kotlinx.json)

    api(libs.quartz)

    testImplementation(libs.kotlin.test.junit)
}
