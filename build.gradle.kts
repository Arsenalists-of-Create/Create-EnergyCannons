plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val mcVersion = stonecutter.current.version

base {
    archivesName.set("${mod.id}-common")
}
version = "${mod.version}+$mcVersion"
group = mod.group

repositories {
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.blamejared.com/")
    maven("https://api.modrinth.com/maven") {
        content { includeGroup("maven.modrinth") }
    }
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.createmod.net")             // Create, Ponder, Flywheel
    maven("https://maven.tterrag.com")               // Registrate
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // Forge Config API Port
    maven("https://mvn.devos.one/releases/")         // Porting Lib
    maven("https://mvn.devos.one/snapshots/")        // Milk Lib, Registrate Fabric
    maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
    maven("https://jitpack.io/")                     // Fabric ASM
    maven("https://maven.realrobotix.me/master/") {  // RPL + CBC
        content { includeGroup("com.rbasamoyai") }
    }
    maven("https://maven.valkyrienskies.org")        // VS2
    maven("https://thedarkcolour.github.io/KotlinForForge/") // Kotlin for Forge
    maven("https://maven.ryanhcode.dev/releases")    // Sable Companion (Aeronautics)
}

architectury {
    common(stonecutter.tree.branches.mapNotNull {
        if (stonecutter.current.project !in it) null
        else it.project.findProperty("loom.platform")?.toString()
    })
}

loom {
    silentMojangMappingsLicense()
}

// Compile-only stubs for Forge-native interfaces referenced by Create's class
// hierarchy. Excluded from the final jar; at runtime Forge supplies the real impls.
sourceSets {
    create("stubs") {
        java.srcDir("src/stubs/java")
    }
    main {
        compileClasspath += sourceSets["stubs"].output
    }
}
tasks.named<JavaCompile>("compileJava") {
    dependsOn(tasks.named("compileStubsJava"))
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$mcVersion:${mod.dep("parchment")}@zip")
    })

    // Fabric Loader on common — only for the @Environment annotations, which get
    // remapped to the right thing on each platform. Don't reference other Fabric
    // Loader classes from common code.
    modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")

    // Architectury API
    modImplementation("dev.architectury:architectury:${mod.dep("architectury_api")}")

    // JSR-305 annotations referenced by Create/CBC class files
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")

    // Mixin Extras (shared)
    compileOnly("io.github.llamalad7:mixinextras-common:${mod.dep("mixin_extras")}")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${mod.dep("mixin_extras")}")

    if (stonecutter.eval(mcVersion, "<1.21")) {
        // 1.20.1 deps: Create has both Forge (slim) and Fabric ports; CBC and friends
        // are on the Fabric Modrinth listing for the common-side compile-only artifacts.
        modCompileOnly("com.simibubi.create:create-$mcVersion:${mod.dep("create")}:slim") { isTransitive = false }
        modCompileOnly("net.createmod.ponder:Ponder-Forge-$mcVersion:${mod.dep("ponder")}") { isTransitive = false }
        modCompileOnly("dev.engine-room.flywheel:flywheel-forge-api-$mcVersion:${mod.dep("flywheel")}") { isTransitive = false }
        modCompileOnly("com.tterrag.registrate:Registrate:${mod.dep("registrate")}") { isTransitive = false }

        modCompileOnly("maven.modrinth:create-big-cannons:${mod.dep("cbc_fabric_id")}") { isTransitive = false }
        modCompileOnly("maven.modrinth:rpl:${mod.dep("rpl")}") { isTransitive = false }

        // Valkyrien Skies 2 (no 1.21.1 build yet)
        modCompileOnly("org.joml:joml-primitives:1.10.0") { isTransitive = false }
        modCompileOnly("org.valkyrienskies.core:api:${mod.dep("vs_core")}") { isTransitive = false }
        modCompileOnly("org.valkyrienskies.core:impl:${mod.dep("vs_core")}") { isTransitive = false }
        modCompileOnly("org.valkyrienskies.core:util:${mod.dep("vs_core")}") { isTransitive = false }
        modCompileOnly("org.valkyrienskies:valkyrienskies-120-forge:${mod.dep("vs2")}") { isTransitive = false }

        modCompileOnly("mezz.jei:jei-$mcVersion-common-api:${mod.dep("jei")}") { isTransitive = false }
        modCompileOnly("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${mod.dep("forge_config_api_port")}") { isTransitive = false }
    } else {
        // 1.21.1+ deps: NeoForge-only. Create bundles Catnip internally but Registrate
        // and Ponder are external runtime deps — we pull them in at compile too so the
        // common code (CECBlocks, CECItems, gated Ponder classes) resolves.
        // NeoForge itself is needed compile-only because catnip's ConfigBase
        // transitively references net.neoforged.neoforge.common.ModConfigSpec.
        modCompileOnly("net.neoforged:neoforge:${mod.dep("neoforge_loader")}")
        modCompileOnly("com.simibubi.create:create-$mcVersion:${mod.dep("create")}") { isTransitive = false }
        modCompileOnly("com.tterrag.registrate:Registrate:${mod.dep("registrate")}") { isTransitive = false }
        modCompileOnly("net.createmod.ponder:ponder-neoforge:${mod.dep("ponder")}") { isTransitive = false }
        modCompileOnly("dev.engine-room.flywheel:flywheel-neoforge-api-$mcVersion:${mod.dep("flywheel")}") { isTransitive = false }


        modCompileOnly("com.rbasamoyai:createbigcannons:5.11.3-dev+mc.1.21.1-build.286") { isTransitive = false }
        modCompileOnly("maven.modrinth:rpl:hZ6B2Z0x") { isTransitive = false }

        modCompileOnly("fuzs.forgeconfigapiport:forgeconfigapiport-neoforge:${mod.dep("forge_config_api_port")}") { isTransitive = false }
        // No VS2 on 1.21.1 (compat/vs2 package gated <1.21-only in source)
        // No JEI on 1.21.1 yet (deps.jei is TODO)


        compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-$mcVersion:1.5.0")
    }
}

java {
    withSourcesJar()
    val javaVersion = if (stonecutter.eval(mcVersion, ">=1.20.5"))
        JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
}

tasks.withType<JavaCompile>().configureEach {
    options.release = if (stonecutter.eval(mcVersion, ">=1.20.5")) 21 else 17
}

tasks.build {
    group = "versioned"
    description = "Must run through 'chiseledBuild'"
}
