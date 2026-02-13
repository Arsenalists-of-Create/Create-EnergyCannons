package net.arsenalists.createenergycannons.compat;

import net.minecraftforge.fml.ModList;

public enum Mods {
    VALKYRIENSKIES("valkyrienskies");

    private final String id;

    Mods(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public boolean isLoaded() {
        return ModList.get().isLoaded(this.id);
    }
}
