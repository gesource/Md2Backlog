pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "md2backlog"

include(":core")
include(":cli")
include(":js-app")
include(":desktop-app")
