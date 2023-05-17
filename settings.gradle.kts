pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}


rootProject.name = "OpenVoice"
include (":app")
includeBuild("plugin-version")
include(":lib-core")
include(":lib-player")
include(":lib-web3")
