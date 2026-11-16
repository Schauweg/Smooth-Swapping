
pluginManagement {
    repositories {
        mavenLocal()
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases/")
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("gg.meza.stonecraft") version "1.14+"
    id("dev.kikugie.stonecutter") version "0.10-alpha.10"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    shared {
        fun mc(version: String, vararg loaders: String) {
            for (it in loaders) version("$version-$it", version)
        }

        mc("1.19", "fabric", "forge")
        mc("1.19.3", "fabric", "forge")
        mc("1.19.4", "fabric", "forge")
        mc("1.20", "fabric", "forge")
        mc("1.20.2", "fabric", "forge")
        mc("1.20.6", "fabric", "neoforge", "forge")
        mc("1.21", "fabric", "neoforge", "forge")
        mc("1.21.2", "fabric", "neoforge")
        mc("1.21.5", "fabric", "neoforge", "forge")
        mc("1.21.6", "fabric", "neoforge", "forge")
        mc("1.21.9", "fabric", "neoforge", "forge")
        mc("1.21.11", "fabric", "neoforge", "forge")
        mc("26.1", "fabric", "neoforge", "forge")
        mc("26.2", "fabric", "neoforge", "forge")
        mc("26.3", "fabric", "neoforge", "forge")

        vcsVersion = "1.21.11-fabric"
    }

    create(rootProject)
}

rootProject.name = "Smooth Swapping"
