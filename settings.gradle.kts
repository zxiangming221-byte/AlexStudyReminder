pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://repo.huaweicloud.com/repository/maven") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://repo.huaweicloud.com/repository/maven") }
    }
}

rootProject.name = "AlexStudyReminder"
include(":app")
