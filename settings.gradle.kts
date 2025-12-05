rootProject.name = "Snowball"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":common")
include(":domain")
include(":infrastructure")
include(":application")
