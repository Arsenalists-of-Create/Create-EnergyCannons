package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.content.contraptions.ContraptionType;
import net.arsenalists.createenergycannons.content.cannons.laser.LaserMountedCannonContraption;
import rbasamoyai.createbigcannons.CreateBigCannons;

public class CECContraptionTypes {

    public static final ContraptionType MOUNTED_LASER_CANNON = ContraptionType.register(CreateBigCannons.resource("mounted_laser_cannon").toString(), LaserMountedCannonContraption::new);

    public static void register() {
        CreateBigCannons.LOGGER.info("Registering Contraption Types");
    }

}
