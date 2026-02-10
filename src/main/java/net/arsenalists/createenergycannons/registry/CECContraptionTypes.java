package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedEnergyCannonContraption;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import rbasamoyai.createbigcannons.CreateBigCannons;

@Mod.EventBusSubscriber(modid = CECMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CECContraptionTypes {

    public static ContraptionType MOUNTED_LASER_CANNON;
    public static ContraptionType COILGUN;
    public static ContraptionType RAIL_CANNON;

    private static final ResourceLocation MOUNTED_LASER_CANNON_ID = CECMod.resource("mounted_laser_cannon");
    private static final ResourceLocation COILGUN_ID = CECMod.resource("coilgun");
    private static final ResourceLocation RAIL_CANNON_ID = CECMod.resource("rail_cannon");
    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        // Only run for Create's contraption type registry
        event.register(CreateBuiltInRegistries.CONTRAPTION_TYPE.key(), helper -> {
            MOUNTED_LASER_CANNON = new ContraptionType(MountedLaserCannonContraption::new);
            helper.register(MOUNTED_LASER_CANNON_ID, MOUNTED_LASER_CANNON);
        });
        event.register(CreateBuiltInRegistries.CONTRAPTION_TYPE.key(), helper -> {
            COILGUN = new ContraptionType(MountedEnergyCannonContraption::new);
            helper.register(COILGUN_ID,COILGUN);
        });
        event.register(CreateBuiltInRegistries.CONTRAPTION_TYPE.key(),helper -> {
            RAIL_CANNON = new ContraptionType(MountedEnergyCannonContraption::new);
            helper.register(RAIL_CANNON_ID,RAIL_CANNON);
        });
    }
}



