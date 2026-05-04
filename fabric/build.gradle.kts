@file:Suppress("UnstableApiUsage")

plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
    id("com.gradleup.shadow")
}

val loader = prop("loom.platform")!!
val mcVersion: String = stonecutter.current.version
val common: Project = requireNotNull(stonecutter.node.sibling("")?.project) {
    "No common project for $project"
}

version = "${mod.version}+$mcVersion"
base {
    archivesName.set("${mod.id}-$loader")
}
architectury {
    platformSetupLoomIde()
    fabric()
}

repositories {
    maven("https://maven.parchmentmc.org")
    maven("https://maven.architectury.dev/")
    maven("https://maven.createmod.net")
    maven("https://maven.tterrag.com")
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.realrobotix.me/master/") { content { includeGroup("com.rbasamoyai") } }
    maven("https://mvn.devos.one/releases/")
    maven("https://mvn.devos.one/snapshots/")
    maven("https://maven.jamieswhiteshirt.com/libs-release")
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
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
    named("developmentFabric").get().extendsFrom(commonBundle)
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$mcVersion:${common.mod.dep("parchment")}@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:${common.mod.dep("fabric_loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${common.mod.dep("fabric_api")}")
    modImplementation("dev.architectury:architectury-fabric:${common.mod.dep("architectury_api")}")

    // Create Fabric — single dependency, everything else pulled in transitively
    // (porting_lib, flywheel, ponder, registrate, forge-config-api-port, milk-lib, etc.)
    modImplementation("com.simibubi.create:create-fabric:${common.mod.dep("create_fabric")}")

    // Create Big Cannons (Fabric)
    modImplementation("maven.modrinth:create-big-cannons:${common.mod.dep("cbc_fabric_id")}")

    // RPL is JiJ'd in CBC but Loom doesn't extract JiJ jars for dev — must declare explicitly
    modRuntimeOnly("com.rbasamoyai:ritchiesprojectilelib:${common.mod.dep("rpl_fabric")}") { isTransitive = false }

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionFabric")) { isTransitive = false }
}

loom {
    runConfigs.all {
        isIdeConfigGenerated = true
        runDir = "../../../run"
    }
}

java {
    withSourcesJar()
    val javaVersion = if (stonecutter.eval(mcVersion, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}

tasks.processResources {
    properties(listOf("fabric.mod.json"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to common.mod.prop("mc_dep_fabric")
    )
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
    exclude("architectury.common.json")
    exclude("architectury.common.marker")
}

tasks.remapJar {
    injectAccessWidener = true
    inputFile.set(tasks.shadowJar.get().archiveFile)
    archiveClassifier = null
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    archiveClassifier = "dev"
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
