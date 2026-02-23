package net.arsenalists.createenergycannons.compat;

import dev.architectury.platform.Platform;

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
        return Platform.isModLoaded(this.id);
    }
}
