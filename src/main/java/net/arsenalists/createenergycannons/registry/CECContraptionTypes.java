package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.content.contraptions.ContraptionType;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import rbasamoyai.createbigcannons.CreateBigCannons;

public class CECContraptionTypes {

    public static final ContraptionType MOUNTED_LASER_CANNON = ContraptionType.register(CECMod.resource("mounted_laser_cannon").toString(), MountedLaserCannonContraption::new);

    public static void register() {
        CreateBigCannons.LOGGER.info("Registering Contraption Types");
    }

}
