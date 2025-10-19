pluginManagement {
    repositories {
        google() // ⚡️ Giữ trống, không giới hạn nhóm
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Project_btl"
include(":app")
include(":OrderManagerFirebase")
