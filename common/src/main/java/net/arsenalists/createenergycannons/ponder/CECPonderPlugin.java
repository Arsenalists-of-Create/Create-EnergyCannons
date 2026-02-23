package net.arsenalists.createenergycannons.ponder;

import net.arsenalists.createenergycannons.CECMod;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CECPonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return CECMod.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CECPonderIndex.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        CECPonderTags.register(helper);
    }
}
