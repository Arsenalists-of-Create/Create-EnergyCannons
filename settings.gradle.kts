pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.6"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    create(rootProject) {
        // Root `src/` functions as the 'common' project. Per-version dep pins
        // live in versions/<version>/gradle.properties.
        versions("1.20.1", "1.21.1")
        branch("fabric")   { versions("1.20.1") } // Create has no 1.21.1 Fabric build
        branch("forge")    { versions("1.20.1") } // Create dropped Forge after 1.20.1
        branch("neoforge") { versions("1.21.1") } // Only viable 1.21.1 loader for Create
    }
}

rootProject.name = "createenergycannons"
