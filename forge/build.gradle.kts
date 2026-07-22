@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!

// Repos must be declared per-subproject; root build.gradle.kts repos don't propagate.
val minecraft: String = stonecutter.current.version
val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

version = "${mod.version}+$minecraft"
base {
    archivesName.set("${mod.id}-$loader")
}

architectury {
    platformSetupLoomIde()
    forge()
}

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.architectury.dev/")
    maven("https://maven.createmod.net")
    maven("https://maven.tterrag.com")
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.realrobotix.me/master/") { content { includeGroup("com.rbasamoyai") } }
    maven("https://maven.valkyrienskies.org")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
    maven("https://maven.blamejared.com/")
}

val commonBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
}

configurations {
    compileClasspath.get().extendsFrom(commonBundle)
    runtimeClasspath.get().extendsFrom(commonBundle)
    get("developmentForge").extendsFrom(commonBundle)
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraft:${common.mod.dep("parchment")}@zip")
    })

    "forge"("net.minecraftforge:forge:${common.mod.dep("forge")}")

    modImplementation("dev.architectury:architectury-forge:${common.mod.dep("architectury_api")}")

    // Create (Forge)
    modImplementation("com.simibubi.create:create-${stonecutter.current.version}:${common.mod.dep("create")}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:Ponder-Forge-${stonecutter.current.version}:${common.mod.dep("ponder")}") { isTransitive = false }
    modCompileOnly("dev.engine-room.flywheel:flywheel-forge-api-${stonecutter.current.version}:${common.mod.dep("flywheel")}") { isTransitive = false }
    modRuntimeOnly("dev.engine-room.flywheel:flywheel-forge-${stonecutter.current.version}:${common.mod.dep("flywheel")}") { isTransitive = false }
    modImplementation("com.tterrag.registrate:Registrate:${common.mod.dep("registrate")}") { isTransitive = false }

    // Mixin Extras (Forge variant)
    implementation("io.github.llamalad7:mixinextras-forge:${common.mod.dep("mixin_extras")}") { isTransitive = false }

    // Create Big Cannons (Forge)
    modImplementation("maven.modrinth:create-big-cannons:${common.mod.dep("cbc")}") { isTransitive = false }
    modImplementation("maven.modrinth:rpl:${common.mod.dep("rpl")}") { isTransitive = false }

    // Valkyrien Skies (Forge) - matches Eureka's proven Architectury setup
    modApi("org.valkyrienskies:valkyrienskies-120-forge:${common.mod.dep("vs2")}") { isTransitive = false }
    implementation("org.valkyrienskies.core:api:${common.mod.dep("vs_core")}")
    implementation("org.valkyrienskies.core:impl:${common.mod.dep("vs_core")}")
    implementation("org.valkyrienskies.core:util:${common.mod.dep("vs_core")}")
    implementation("org.joml:joml-primitives:1.10.0") { isTransitive = false }
    implementation("thedarkcolour:kotlinforforge:${common.mod.dep("kotlin_for_forge")}")

    // JEI (Forge)
    modCompileOnly("mezz.jei:jei-${stonecutter.current.version}-forge-api:${common.mod.dep("jei")}") { isTransitive = false }
    modRuntimeOnly("mezz.jei:jei-${stonecutter.current.version}-forge:${common.mod.dep("jei")}") { isTransitive = false }

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionForge")) { isTransitive = false }
}

loom {
    forge {
        mixinConfig("createenergycannons.mixins.json")
        mixinConfig("createenergycannons.forge.mixins.json")
    }
}

java {
    withSourcesJar()
    val java = if (stonecutter.eval(minecraft, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = java
    sourceCompatibility = java
}

tasks.jar {
    archiveClassifier = "dev"
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
    exclude("architectury.common.json")
    exclude("architectury.common.marker")
}

tasks.remapJar {
    injectAccessWidener = true
    input = tasks.shadowJar.get().archiveFile
    archiveClassifier = null
    dependsOn(tasks.shadowJar)
}

tasks.processResources {
    properties(listOf("META-INF/mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to common.mod.prop("mc_dep_forgelike")
    )
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}

tasks.register<Copy>("buildAndCollect") {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
    from(tasks.remapJar.get().archiveFile, tasks.remapSourcesJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}/$loader"))
    dependsOn("build")
}
