rootProject.name = "Snowball"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include(":domain")
include(":infrastructure:database")
include(":infrastructure:kis-api")
include(":presentation")
include(":application")
