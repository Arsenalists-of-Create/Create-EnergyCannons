package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.api.contraption.ContraptionType;
import net.arsenalists.createenergycannons.CECMod;

import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedEnergyCannonContraption;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public final class CECCreateRegistries {

    private CECCreateRegistries() {}

    public static void registerContraptionTypes(BiConsumer<ResourceLocation, ContraptionType> registrar) {
        CECContraptionTypes.MOUNTED_LASER_CANNON =
                new ContraptionType(MountedLaserCannonContraption::new);
        registrar.accept(
                CECMod.resource("mounted_laser_cannon"),
                CECContraptionTypes.MOUNTED_LASER_CANNON
        );

        CECContraptionTypes.COILGUN =
                new ContraptionType(MountedEnergyCannonContraption::new);
        registrar.accept(
                CECMod.resource("coilgun"),
                CECContraptionTypes.COILGUN
        );

        CECContraptionTypes.RAIL_CANNON =
                new ContraptionType(MountedEnergyCannonContraption::new);
        registrar.accept(
                CECMod.resource("rail_cannon"),
                CECContraptionTypes.RAIL_CANNON
        );
    }

    public static void registerArmInteractionPointTypes(
            BiConsumer<ResourceLocation, CECArmInteractionPointTypes.EnergyCannonMountType> registrar
    ) {
        CECArmInteractionPointTypes.ENERGY_CANNON_MOUNT =
                new CECArmInteractionPointTypes.EnergyCannonMountType();
        registrar.accept(
                CECMod.resource("energy_cannon_mount"),
                CECArmInteractionPointTypes.ENERGY_CANNON_MOUNT
        );
    }
}