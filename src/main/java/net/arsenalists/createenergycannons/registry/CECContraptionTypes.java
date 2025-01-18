package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.content.contraptions.ContraptionType;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.cannons.laser.MountedLaserCannonContraption;
import net.arsenalists.createenergycannons.content.cannons.magnetic.railgun.MountedRailCannonContrpation.MountedRailCannonContraption;
import rbasamoyai.createbigcannons.CreateBigCannons;

public class CECContraptionTypes {

    public static final ContraptionType MOUNTED_LASER_CANNON = ContraptionType.register(CECMod.resource("mounted_laser_cannon").toString(), MountedLaserCannonContraption::new);
    public static final ContraptionType MOUNTED_RAIL_CANNON = ContraptionType.register(CECMod.resource("mounted_rail_cannon").toString(), MountedRailCannonContraption::new);

    public static void register() {
        CreateBigCannons.LOGGER.info("Registering Contraption Types");
    }

}
