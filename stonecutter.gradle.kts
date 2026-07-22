plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.14.473" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "8.3.6" apply false
}
stonecutter active "1.20.1" /* [SC] DO NOT EDIT */

// Builds every (loader, version) into build/libs/{mod.version}/{loader}.
stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

// Per-loader: chiseledBuildFabric, chiseledBuildForge, ...
for (it in stonecutter.tree.branches) {
    if (it.id.isEmpty()) continue
    val loader = it.id.replaceFirstChar { c -> c.uppercaseChar() }
    stonecutter registerChiseled tasks.register("chiseledBuild$loader", stonecutter.chiseled) {
        group = "project"
        versions { branch, _ -> branch == it.id }
        ofTask("buildAndCollect")
    }
}
