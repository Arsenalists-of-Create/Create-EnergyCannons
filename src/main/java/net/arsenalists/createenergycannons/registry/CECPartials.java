package net.arsenalists.createenergycannons.registry;



import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.arsenalists.createenergycannons.CECMod;

public class CECPartials {
    public static void register() {
        CECMod.getLogger().info("Registering Partials");
    }

    public static final PartialModel MAGNETIC_SLED = block("magnetic_sled");
    public static final PartialModel LASER_CUBE = block("laser_cube");

    private static PartialModel block(String path) {
        return PartialModel.of(CECMod.resource("block/" + path));
    }

    private static PartialModel entity(String path) {
        return  PartialModel.of(CECMod.resource("entity/" + path));
    }
}
