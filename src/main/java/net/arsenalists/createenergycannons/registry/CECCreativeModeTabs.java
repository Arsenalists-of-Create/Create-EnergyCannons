package net.arsenalists.createenergycannons.registry;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.utility.Components;
import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static net.arsenalists.createenergycannons.CECMod.REGISTRATE;

public class CECCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CECMod.MODID);

    public static final RegistryObject<CreativeModeTab> CEC_CREATIVE_TAB = addTab("createenergycannons", "Create Energy Cannons",
            CECBlocks.BATTERY_BLOCK::asStack);


    public static RegistryObject<CreativeModeTab> addTab(String id, String name, Supplier<ItemStack> icon) {
        String itemGroupId = "itemGroup." + CECMod.MODID + "." + id;
        REGISTRATE.addRawLang(itemGroupId, name);
        CreativeModeTab.Builder tabBuilder = CreativeModeTab.builder()
                .icon(icon)
                .displayItems(CECCreativeModeTabs::displayItems)
                .title(Components.translatable(itemGroupId))
                .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey());
        return CREATIVE_MODE_TABS.register(id, tabBuilder::build);
    }

    private static void displayItems(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
        pOutput.accept(CECBlocks.ENERGY_CANNON_MOUNT);
        pOutput.accept(CECBlocks.BATTERY_BLOCK);
        pOutput.accept(CECBlocks.LASER);
        //pOutput.accept(CECBlocks.NETHERSTEEL_SCREW_BREECH);
        //pOutput.accept(CECBlocks.RAILGUN_BARREL);
        //pOutput.accept(CECBlocks.NETHERSTEEL_COILGUN_BARREL);
        //pOutput.accept(CECBlocks.STEEL_SLIDING_BREECH);
        //pOutput.accept(CECBlocks.STEEL_RAIL_SCREW_BREECH);
        //pOutput.accept(CECBlocks.STEEL_RAIL_QUICKFIRING_BREECH);
        //pOutput.accept(CECBlocks.STEEL_COILGUN_BARREL);
        //pOutput.accept(CECItems.MAGNETIC_SLED);
    }


    public static void register(IEventBus eventBus) {
        CECMod.getLogger().info("Registering CreativeTabs!");
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
