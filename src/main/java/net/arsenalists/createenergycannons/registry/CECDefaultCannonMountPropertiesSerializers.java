package net.arsenalists.createenergycannons.registry;

import net.arsenalists.createenergycannons.CECMod;
import rbasamoyai.createbigcannons.cannon_control.config.CannonMountPropertiesHandler;
import rbasamoyai.createbigcannons.cannon_control.config.SimpleBlockMountProperties;

public class CECDefaultCannonMountPropertiesSerializers {

    public static void init() {
        CECMod.getLogger().info("Registering default cannon mount properties serializers");
        CannonMountPropertiesHandler.registerBlockMountSerializer(CECBlockEntity.ENERGY_CANNON_MOUNT.get(), new SimpleBlockMountProperties.Serializer());
    }

}
