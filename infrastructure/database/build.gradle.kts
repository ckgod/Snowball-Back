plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":domain"))

    implementation(libs.bundles.exposed)
    implementation(libs.h2.database)

    testImplementation(libs.kotlin.test.junit)
}
