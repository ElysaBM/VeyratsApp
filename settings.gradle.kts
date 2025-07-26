pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        // If you're using GetStream
        maven {
            url = uri("https://dl.stream.io/android")
            content {
                includeGroup("io.getstream")
            }
        }
    }
}


rootProject.name = "My Application"
include(":app")
