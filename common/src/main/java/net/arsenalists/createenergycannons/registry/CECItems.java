package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.foundation.data.AssetLookup;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.arsenalists.createenergycannons.CECMod;
import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.SledItem;

public class CECItems {

    public static final ItemEntry<SledItem> MAGNETIC_SLED = CECMod.REGISTRATE
            .item("magnetic_sled", SledItem::new)
            .model(AssetLookup.existingItemModel())
            .register();

    public static void register() {
        CECMod.getLogger().info("Registering Items");
    }
}
