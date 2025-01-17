package net.arsenalists.createenergycannons.registry;

import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.CannonContraptionTypeRegistry;
import rbasamoyai.createbigcannons.cannon_control.cannon_types.ICannonContraptionType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CECCannonContraptionTypes implements ICannonContraptionType {
    LASER;

    private static final Map<ResourceLocation, CECCannonContraptionTypes> BY_ID = Arrays.stream(values()).collect(Collectors.toMap(CECCannonContraptionTypes::getId, Function.identity()));
    private final ResourceLocation id;

    CECCannonContraptionTypes() {
        this.id = CECMod.resource(this.name().toLowerCase(Locale.ROOT));
        CannonContraptionTypeRegistry.register(this.id, this);
    }

    public ResourceLocation getId() {
        return this.id;
    }

    @Nullable
    public static CECCannonContraptionTypes byId(ResourceLocation loc) {
        return BY_ID.get(loc);
    }

    public static void register() {
        CECMod.getLogger().info("Registering Cannon Contraption Types");
    }
}