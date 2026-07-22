package net.arsenalists.createenergycannons.config;

/**
 * Loader-neutral replacement for ModConfig.Type - the latter has different
 * package paths between Forge 1.20.1 (net.minecraftforge.fml.config.ModConfig)
 * and NeoForge 1.21.1 (no equivalent in the merged jar; configs are registered
 * differently). Loader-specific code (CECModForge, CECModFabric, CECModNeoForge)
 * maps these to the platform-native enum at registration time.
 */
public enum ConfigType {
    CLIENT,
    COMMON,
    SERVER
}
