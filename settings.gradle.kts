pluginManagement {
    repositories {
        //maven { url "https://jitpack.io" }

        google()
        mavenCentral()
        gradlePluginPortal()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }



    }
}

rootProject.name = "SpendSmart"
include(":app")
