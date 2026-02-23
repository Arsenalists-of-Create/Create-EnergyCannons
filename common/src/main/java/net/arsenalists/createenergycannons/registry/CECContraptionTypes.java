package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedEnergyCannonContraption;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class CECContraptionTypes {

    public static ContraptionType MOUNTED_LASER_CANNON;
    public static ContraptionType COILGUN;
    public static ContraptionType RAIL_CANNON;

    private static final ResourceLocation MOUNTED_LASER_CANNON_ID = CECMod.resource("mounted_laser_cannon");
    private static final ResourceLocation COILGUN_ID = CECMod.resource("coilgun");
    private static final ResourceLocation RAIL_CANNON_ID = CECMod.resource("rail_cannon");

    public static void register() {
        MOUNTED_LASER_CANNON = new ContraptionType(MountedLaserCannonContraption::new);
        Registry.register(CreateBuiltInRegistries.CONTRAPTION_TYPE, MOUNTED_LASER_CANNON_ID, MOUNTED_LASER_CANNON);

        COILGUN = new ContraptionType(MountedEnergyCannonContraption::new);
        Registry.register(CreateBuiltInRegistries.CONTRAPTION_TYPE, COILGUN_ID, COILGUN);

        RAIL_CANNON = new ContraptionType(MountedEnergyCannonContraption::new);
        Registry.register(CreateBuiltInRegistries.CONTRAPTION_TYPE, RAIL_CANNON_ID, RAIL_CANNON);
    }
}
