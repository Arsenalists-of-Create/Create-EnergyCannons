package net.arsenalists.createenergycannons.registry;

import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.CannonContraptionTypeRegistry;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;

import java.util.Locale;

public enum CECCannonContraptionTypes implements ICannonContraptionType {
    RAIL_CANNON,
    LASER;

    private final ResourceLocation id;

    CECCannonContraptionTypes() {
        this.id = CECMod.resource(this.name().toLowerCase(Locale.ROOT));
        CannonContraptionTypeRegistry.register(this.id, this);
    }

    public ResourceLocation getId() {
        return this.id;
    }


    public static void register() {
        CECMod.getLogger().info("Registering Cannon Contraption Types");
    }
}