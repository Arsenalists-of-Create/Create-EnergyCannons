package net.arsenalists.createenergycannons.registry;

import com.jozufozu.flywheel.core.PartialModel;
import net.arsenalists.createenergycannons.CECMod;

public class CECPartials {
    public static void register() {
        CECMod.getLogger().info("Registering Partials");
    }

    public static final PartialModel MAGNETIC_SLED = block("magnetic_sled");
    public static final PartialModel LASER_CUBE = block("laser_cube");

    private static PartialModel block(String path) {
        return new PartialModel(CECMod.resource("block/" + path));
    }

    private static PartialModel entity(String path) {
        return new PartialModel(CECMod.resource("entity/" + path));
    }
}
