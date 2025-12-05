plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":common"))

    // 도메인 모델을 위한 최소한의 의존성
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
}
