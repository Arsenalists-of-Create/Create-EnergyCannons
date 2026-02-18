package net.arsenalists.createenergycannons.ponder;

import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.arsenalists.createenergycannons.registry.CECBlocks;
import net.arsenalists.createenergycannons.registry.CECItems;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CECPonderIndex {

    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?>> HELPER =
                helper.withKeyFunction(RegistryEntry::getId);

        // Energy Cannon Mount
        HELPER.forComponents(CECBlocks.ENERGY_CANNON_MOUNT)
                .addStoryBoard("energy_mount", CECPonderScenes::energyMountSetup, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("energy_mount", CECPonderScenes::energyMountPower, CECPonderTags.ENERGY_CANNONS);

        // Laser Cannon
        HELPER.forComponents(CECBlocks.LASER)
                .addStoryBoard("laser_cannon", CECPonderScenes::laserBasics, CECPonderTags.ENERGY_CANNONS);

        // Magnetic Cannons (Railgun & Coilgun combined)
        HELPER.forComponents(CECBlocks.NETHERSTEEL_RAILGUN_BARREL)
                .addStoryBoard("energy_cannons", CECPonderScenes::magneticCannonBasics, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("magnetic_sleds", CECPonderScenes::magneticSledAssembly, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("energy_cannons", CECPonderScenes::railgunVsCoilgun, CECPonderTags.ENERGY_CANNONS);

        // Railgun - Steel (same scenes)
        HELPER.forComponents(CECBlocks.STEEL_RAILGUN_BARREL)
                .addStoryBoard("energy_cannons", CECPonderScenes::magneticCannonBasics, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("magnetic_sleds", CECPonderScenes::magneticSledAssembly, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("energy_cannons", CECPonderScenes::railgunVsCoilgun, CECPonderTags.ENERGY_CANNONS);

        // Coil Gun - Steel (same scenes)
        HELPER.forComponents(CECBlocks.STEEL_COILGUN_BARREL)
                .addStoryBoard("energy_cannons", CECPonderScenes::magneticCannonBasics, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("magnetic_sleds", CECPonderScenes::magneticSledAssembly, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("energy_cannons", CECPonderScenes::railgunVsCoilgun, CECPonderTags.ENERGY_CANNONS);

        // Coil Gun - Nethersteel (same scenes)
        HELPER.forComponents(CECBlocks.NETHERSTEEL_COILGUN_BARREL)
                .addStoryBoard("energy_cannons", CECPonderScenes::magneticCannonBasics, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("magnetic_sleds", CECPonderScenes::magneticSledAssembly, CECPonderTags.ENERGY_CANNONS)
                .addStoryBoard("energy_cannons", CECPonderScenes::railgunVsCoilgun, CECPonderTags.ENERGY_CANNONS);

        // Magnetic Sled Item
        HELPER.forComponents(CECItems.MAGNETIC_SLED)
                .addStoryBoard("magnetic_sleds", CECPonderScenes::magneticSledAssembly, CECPonderTags.ENERGY_CANNONS);
    }
}
