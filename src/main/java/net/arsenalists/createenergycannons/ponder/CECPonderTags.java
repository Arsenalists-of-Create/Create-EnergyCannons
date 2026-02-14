package net.arsenalists.createenergycannons.ponder;

import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.registry.CECBlocks;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CECPonderTags {

    public static final ResourceLocation ENERGY_CANNONS = CECMod.resource("energy_cannons");

    public static void register(PonderTagRegistrationHelper<ResourceLocation> helper) {
        helper.registerTag(ENERGY_CANNONS)
                .addToIndex()
                .item(CECBlocks.ENERGY_CANNON_MOUNT.get())
                .title("Energy Cannons")
                .description("Powerful energy-based weaponry powered by Forge Energy")
                .register();

        helper.addToTag(ENERGY_CANNONS)
                .add(CECBlocks.ENERGY_CANNON_MOUNT.getId())
                .add(CECBlocks.LASER.getId())
                .add(CECBlocks.RAILGUN_BARREL.getId())
                .add(CECBlocks.STEEL_COILGUN_BARREL.getId())
                .add(CECBlocks.NETHERSTEEL_COILGUN_BARREL.getId());
    }
}
