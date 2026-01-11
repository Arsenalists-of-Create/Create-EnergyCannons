package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.api.contraption.ContraptionType;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = CECMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CECContraptionTypes {

    public static ContraptionType MOUNTED_LASER_CANNON;

    private static final ResourceLocation MOUNTED_LASER_CANNON_ID =
            CECMod.resource("mounted_laser_cannon");

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        // Only run for Create's contraption type registry
        event.register(CreateBuiltInRegistries.CONTRAPTION_TYPE.key(), helper -> {
            MOUNTED_LASER_CANNON = new ContraptionType(MountedLaserCannonContraption::new);
            helper.register(MOUNTED_LASER_CANNON_ID, MOUNTED_LASER_CANNON);
        });
    }
}
