plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(project(":domain"))

    api(libs.bundles.exposed)
    api(libs.h2.database)

    testImplementation(libs.kotlin.test.junit)
}
