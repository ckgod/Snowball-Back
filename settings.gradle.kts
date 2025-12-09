rootProject.name = "Snowball"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":domain")
include(":data")
include(":presentation")
include(":application")
