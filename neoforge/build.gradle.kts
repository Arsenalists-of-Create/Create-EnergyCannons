@file:Suppress("UnstableApiUsage")

import java.net.URL
import java.net.URLEncoder
import java.util.zip.ZipFile

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
    neoForge()
}

repositories {
    mavenCentral()                                    // imgui-java (io.github.spair)
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.architectury.dev/")
    maven("https://maven.createmod.net")              // Create, Ponder, Flywheel, Catnip
    maven("https://maven.ithundxr.dev/snapshots")     // Registrate (official Create addon recipe)
    maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } }
    maven("https://maven.realrobotix.me/master/") { content { includeGroup("com.rbasamoyai") } }
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/")
    maven("https://maven.blamejared.com/")
    maven("https://maven.ryanhcode.dev/releases")     // Sable Companion (Sable / Create Aeronautics integration)
    maven("https://modmaven.dev/")                    // moonflower molang-compiler (used by Veil)
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
    named("developmentNeoForge").get().extendsFrom(commonBundle)
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$mcVersion:${common.mod.dep("parchment")}@zip")
    })

    "neoForge"("net.neoforged:neoforge:${common.mod.dep("neoforge_loader")}")

    // Architectury API (NeoForge)
    modImplementation("dev.architectury:architectury-neoforge:${common.mod.dep("architectury_api")}")

    // Create (NeoForge 1.21.1) — official addon recipe from
    // https://wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1.
    // The :slim classifier strips Create's runtime-only deps (FTB, JourneyMap,
    // Curios, etc.) so we don't drag in unrelated mods.
    modImplementation("com.simibubi.create:create-$mcVersion:${common.mod.dep("create")}:slim") { isTransitive = false }
    modImplementation("net.createmod.ponder:ponder-neoforge:${common.mod.dep("ponder")}") { isTransitive = false }
    // Catnip: compileOnly only. Ponder JIJ-bundles Catnip, and Loom extracts JIJ
    // into the dev runtime — having two copies trips NeoForge's JPMS split-package
    // check ("Modules ponder and catnip export net.createmod.catnip.net.base").
    // isTransitive=false also needed because Catnip 0.8.54's POM points at a stale
    // flywheel artifact (dev.engine_room.flywheel:1.0.0-beta-fork.10 — gone).
    modCompileOnly("net.createmod.catnip:Catnip-NeoForge-$mcVersion:${common.mod.dep("catnip")}") { isTransitive = false }
    modImplementation("com.tterrag.registrate:Registrate:${common.mod.dep("registrate")}") { isTransitive = false }

    // Flywheel (NeoForge) — per the official Create wiki recipe at
    // wiki.createmod.net/developers/depend-on-create/neoforge-1.21.1, this MUST
    // be plain compileOnly/runtimeOnly (NOT modCompileOnly/modRuntimeOnly).
    // NeoForge 1.21+ mods ship with Mojang names already, so Loom's mod-jar
    // remapping is unwanted — going through it broke Flywheel's mixin config
    // (`@Mixin(targets = "net.minecraft...SkyLightSectionStorage.SkyDataLayerStorageMap")`
    // got mangled, the SkyDataLayerStorageMapAccessor failed to apply, and the
    // indirect backend's LightDataCollector$Fast crashed at first chunk render).
    compileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-$mcVersion:${common.mod.dep("flywheel")}")
    runtimeOnly("dev.engine-room.flywheel:flywheel-neoforge-$mcVersion:${common.mod.dep("flywheel")}")

    // Mixin Extras
    "io.github.llamalad7:mixinextras-neoforge:${common.mod.dep("mixin_extras")}".let {
        implementation(it)
        include(it)
    }

    // Create Big Cannons (NeoForge) + RPL — RPL is JIJ-bundled inside CBC at
    // production runtime but Loom doesn't extract it for dev, so declare it
    // explicitly otherwise CBC fails to load with "ritchiesprojectilelib MISSING".
    // Pulled from rbasamoyai's nightly maven (not Modrinth) so we get the post-5.11.2
    // patches that fix CannonMountInterfaceBlockEntity.addPropagationLocations returning
    // an immutable List.of(...) — that interacted badly with Create Simulated's mixin
    // (.addAll on returned list → UnsupportedOperationException, crashed any cannon mount tick).
    modImplementation("com.rbasamoyai:createbigcannons:5.11.3-dev+mc.1.21.1-build.286") { isTransitive = false }
    modRuntimeOnly("com.rbasamoyai:ritchiesprojectilelib:${common.mod.dep("rpl")}") { isTransitive = false }

    // VS2 — no 1.21.1 build, package gated <1.21-only in source

    // Sable Companion — coordinate transforms for Sable sub-levels (Create Aeronautics
    // is built on Sable, so this also covers Aeronautics ships). The lib ships a safe
    // default impl when Sable mod isn't installed, so calling SableCompanion.INSTANCE is
    // always safe. JiJ via Loom's include() so end users don't need a separate download.
    // Pinned to 1.5.0: Sable 1.1.3's mod metadata declares incompatibility with
    // sablecompanion >1.5.0; Sable refuses to load against 1.6.0. Bump alongside Sable.
    "dev.ryanhcode.sable-companion:sable-companion-common-$mcVersion:1.5.0".let {
        implementation(it)
        include(it)
    }

    // Dev runtime test mods — load in runClient only, not bundled in the published jar.
    // Skipped on CI: these are for local testing against Sable/Aeronautics/Veil and the
    // create-aeronautics bundle download relies on a Loom cache that doesn't exist in CI.
    if (System.getenv("CI") == null) {
        // Sable is the physics/sub-level mod our compat/sable code targets; Create Aeronautics
        // is built on Sable and bundles Create Simulated (no separate Simulated mod exists).
        modRuntimeOnly("maven.modrinth:sable:g8CObHcP")               // Sable 1.1.3+mc1.21.1
        // Create Aeronautics — the published jar is a BUNDLE WRAPPER that nests
        // aeronautics/simulated/offroad as JIJ. NeoForge production extracts JIJ mods
        // automatically; Loom's dev runtime does NOT. We grab the bundle from Modrinth
        // (resolves the deps), then extractBundledMods() pulls the inner jars out at
        // configuration time so modRuntimeOnly can pass them through Loom remapping.
        modRuntimeOnly("maven.modrinth:create-aeronautics:1sv6OtSz")
        extractBundledMods("create-aeronautics", "1sv6OtSz", listOf(
            "dev.eriksonn.aeronautics.aeronautics-neoforge-1.21.1-1.1.3.jar",
            "dev.simulated_team.simulated.simulated-neoforge-1.21.1-1.1.3.jar",
            "dev.ryanhcode.offroad.offroad-neoforge-1.21.1-1.1.3.jar",
        )).forEach { modRuntimeOnly(files(it)) }
        // Veil — required at runtime by Aeronautics/Simulated/Offroad (449 refs in Simulated alone)
        // even though their mods.toml doesn't declare it. Without it, runClient crashes with
        // NoClassDefFoundError: foundry/veil/api/compat/SodiumCompat at mod-load time.
        modRuntimeOnly("maven.modrinth:veil:iIz78CBf")                // Veil 3.6.2

        // Veil's Dear ImGui bindings — JIJ-bundled inside the published Veil jar but Loom
        // doesn't extract JIJ for dev. Use forgeRuntimeLibrary (NOT plain runtimeOnly) so
        // these land on the NeoForge mod classloader; runtimeOnly only adds to Gradle's
        // classpath which the isolated mod classloader can't see, causing
        // ClassNotFoundException: imgui.ImGui at mod-load time.
        "forgeRuntimeLibrary"("io.github.spair:imgui-java-binding:1.88.0")
        "forgeRuntimeLibrary"("io.github.spair:imgui-java-lwjgl3:1.88.0")
        "forgeRuntimeLibrary"("io.github.spair:imgui-java-natives-windows:1.88.0")
        "forgeRuntimeLibrary"("io.github.spair:imgui-java-natives-linux:1.88.0")
        "forgeRuntimeLibrary"("io.github.spair:imgui-java-natives-macos:1.88.0")

        // Veil's other JIJ libs:
        //   - molang-compiler is on modmaven, so use the normal coordinate
        //   - glsl-processor is NOT published anywhere except JIJ'd inside Veil, so
        //     extractVeilJij task (below) pulls it out and adds the extracted jar via files()
        "forgeRuntimeLibrary"("gg.moonflower:molang-compiler:3.1.1.19")
        "forgeRuntimeLibrary"(files(layout.buildDirectory.file("veil-jij/glsl-processor-0.2.3.jar"))
            .builtBy("extractVeilJij"))
    }

    commonBundle(project(common.path, "namedElements")) { isTransitive = false }
    shadowBundle(project(common.path, "transformProductionNeoForge")) { isTransitive = false }
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

// Extract Veil's JIJ'd glsl-processor (not published anywhere as a standalone artifact).
// Same dance as extractBundledMods() but for forgeRuntimeLibrary, not modRuntime — Loom
// doesn't try to remap forgeRuntimeLibrary entries so we can use a normal task.
val extractVeilJij by tasks.registering(Copy::class) {
    val veilJars = configurations.named("modRuntimeClasspathMainMapped").map { cfg ->
        cfg.files.filter { it.name.contains("veil-") && it.name.endsWith(".jar") }
    }
    from(veilJars.map { jars -> jars.map { zipTree(it).matching { include("META-INF/jarjar/glsl-processor-*.jar") } } })
    into(layout.buildDirectory.dir("veil-jij"))
    eachFile { path = name } // strip META-INF/jarjar/ prefix
    includeEmptyDirs = false
}

tasks.named("runClient") { dependsOn(extractVeilJij) }
tasks.named("compileJava") { dependsOn(extractVeilJij) }

/**
 * Extracts named JIJ mod jars from a Modrinth bundle wrapper at CONFIGURATION time
 * (synchronously, in the gradle script body), so the resulting files exist before Loom
 * evaluates modRuntimeOnly(files(...)) and tries to checksum/remap them. Returns the
 * extracted file paths.
 *
 * The bundle jar comes from the Loom remapped-mods cache after we declare modRuntimeOnly
 * for the Modrinth project. We can't depend on a configuration here (Loom hasn't built
 * it yet at config time) — instead we resolve the underlying Modrinth artifact directly.
 */
fun extractBundledMods(modrinthSlug: String, versionId: String, jijNames: List<String>): List<File> {
    val outDir = layout.buildDirectory.dir("bundled-mods").get().asFile
    outDir.mkdirs()
    val outputs = jijNames.map { jij -> outDir.resolve(jij.substringAfterLast('/')) }
    if (outputs.all { it.exists() }) return outputs

    // Pull the bundle jar out of Loom's remapped-mods cache (populated when modRuntimeOnly
    // resolved). On a clean checkout the cache may be empty, so fall back to a direct
    // Modrinth CDN fetch — same artifact either way.
    val cacheDir = file("${gradle.gradleUserHomeDir}/caches/modules-2/files-2.1/maven.modrinth/$modrinthSlug/$versionId")
    val bundleJar = cacheDir.walkTopDown()
        .firstOrNull { it.name == "$modrinthSlug-$versionId.jar" }
        ?: outDir.resolve("$modrinthSlug-$versionId.jar").also { f ->
            if (!f.exists()) {
                logger.lifecycle("Downloading $modrinthSlug bundle from Modrinth...")
                URL("https://cdn.modrinth.com/data/" +
                        URLEncoder.encode(modrinthSlug, "UTF-8") +
                        "/versions/$versionId/" +
                        URLEncoder.encode(f.name, "UTF-8")).openStream().use { input ->
                    f.outputStream().use { output -> input.copyTo(output) }
                }
            }
        }

    val zip = ZipFile(bundleJar)
    try {
        for (jij in jijNames) {
            val entry = zip.getEntry("META-INF/jarjar/$jij")
            if (entry == null) {
                throw GradleException("Bundle $bundleJar does not contain META-INF/jarjar/$jij")
            }
            val out = outDir.resolve(jij.substringAfterLast('/'))
            zip.getInputStream(entry).use { input ->
                out.outputStream().use { output -> input.copyTo(output) }
            }
        }
    } finally {
        zip.close()
    }
    return outputs
}

tasks.processResources {
    properties(listOf("META-INF/neoforge.mods.toml"),
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "minecraft" to common.mod.prop("mc_dep_forgelike")
    )
}

tasks.shadowJar {
    configurations = listOf(shadowBundle)
    archiveClassifier = "dev-shadow"
    exclude("fabric.mod.json")
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
