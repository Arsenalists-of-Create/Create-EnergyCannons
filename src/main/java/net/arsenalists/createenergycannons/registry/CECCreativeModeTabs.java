package net.arsenalists.createenergycannons.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
//? if >=1.21
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

import static net.arsenalists.createenergycannons.CECMod.REGISTRATE;

public class CECCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(CECMod.MODID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> CEC_CREATIVE_TAB = addTab("createenergycannons", "Create Energy Cannons",
            CECBlocks.BATTERY_BLOCK::asStack);


    public static RegistrySupplier<CreativeModeTab> addTab(String id, String name, Supplier<ItemStack> icon) {
        String itemGroupId = "itemGroup." + CECMod.MODID + "." + id;
        REGISTRATE.addRawLang(itemGroupId, name);
        CreativeModeTab.Builder tabBuilder = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .icon(icon)
                .displayItems(CECCreativeModeTabs::displayItems)
                .title(Component.translatable(itemGroupId));
        return CREATIVE_MODE_TABS.register(id, tabBuilder::build);
    }

    private static void displayItems(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        //? if <1.21 {
        /*pOutput.accept(CECBlocks.ENERGY_CANNON_MOUNT);
        pOutput.accept(CECBlocks.BATTERY_BLOCK);
        pOutput.accept(CECBlocks.LASER);
        pOutput.accept(CECBlocks.NETHERSTEEL_RAILGUN_BARREL);
        pOutput.accept(CECBlocks.STEEL_RAILGUN_BARREL);
        pOutput.accept(CECBlocks.STEEL_COILGUN_BARREL);
        pOutput.accept(CECBlocks.NETHERSTEEL_COILGUN_BARREL);
        pOutput.accept(CECItems.MAGNETIC_SLED);
        *///?}
        // 1.21+: tab population happens in register() via REGISTRATE.modifyCreativeModeTab.
    }


    public static void register() {
        CECMod.getLogger().info("Registering CreativeTabs!");
        CREATIVE_MODE_TABS.register();
        // 1.21+: items populate the tab via Registrate's BuildCreativeModeTabContentsEvent
    }
}
