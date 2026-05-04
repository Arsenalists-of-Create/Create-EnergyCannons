package net.arsenalists.createenergycannons.neoforge.client;

import net.arsenalists.createenergycannons.CECMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class SledModelHandler {

    private static final String[] SHELL_IDS = {
            "ap_shell", "ap_shot", "fluid_shell", "he_shell",
            "shrapnel_shell", "smoke_shell", "solid_shot", "drop_mortar_shell"
    };

    public static void onRegisterAdditional(ModelEvent.RegisterAdditional event) {
        for (String shell : SHELL_IDS) {
            event.register(sledMrl(shell + "_sled"));
            if (!shell.equals("ap_shot") && !shell.equals("solid_shot")) {
                event.register(sledMrl(shell + "_head_fuze_sled"));
                event.register(sledMrl(shell + "_base_fuze_sled"));
            }
        }
    }

    private static ModelResourceLocation sledMrl(String name) {
        return ModelResourceLocation.standalone(
                ResourceLocation.fromNamespaceAndPath(CECMod.MODID, "item/" + name));
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();

        for (String shell : SHELL_IDS) {
            ModelResourceLocation itemModelLoc = ModelResourceLocation.inventory(
                    ResourceLocation.fromNamespaceAndPath("createbigcannons", shell));

            BakedModel original = models.get(itemModelLoc);
            if (original == null) continue;

            BakedModel sledModel = models.get(sledMrl(shell + "_sled"));
            BakedModel headFuzeSledModel = models.get(sledMrl(shell + "_head_fuze_sled"));
            BakedModel baseFuzeSledModel = models.get(sledMrl(shell + "_base_fuze_sled"));

            if (sledModel == null) continue;

            models.put(itemModelLoc, new SledOverrideModel(original, sledModel, headFuzeSledModel, baseFuzeSledModel));
        }
        CECMod.getLogger().info("Injected sled model overrides for {} shell types", SHELL_IDS.length);
    }

    private static class SledOverrideModel implements BakedModel {
        private final BakedModel original;
        private final ItemOverrides overrides;

        SledOverrideModel(BakedModel original, BakedModel sledModel,
                          @Nullable BakedModel headFuzeSledModel, @Nullable BakedModel baseFuzeSledModel) {
            this.original = original;
            this.overrides = new SledItemOverrides(original.getOverrides(), sledModel, headFuzeSledModel, baseFuzeSledModel);
        }

        @Override public List<BakedQuad> getQuads(@Nullable BlockState s, @Nullable Direction d, RandomSource r) { return original.getQuads(s, d, r); }
        @Override public boolean useAmbientOcclusion() { return original.useAmbientOcclusion(); }
        @Override public boolean isGui3d() { return original.isGui3d(); }
        @Override public boolean usesBlockLight() { return original.usesBlockLight(); }
        @Override public boolean isCustomRenderer() { return original.isCustomRenderer(); }
        @Override public TextureAtlasSprite getParticleIcon() { return original.getParticleIcon(); }
        @Override public ItemTransforms getTransforms() { return original.getTransforms(); }
        @Override public ItemOverrides getOverrides() { return overrides; }
    }

    private static class SledItemOverrides extends ItemOverrides {
        private final ItemOverrides original;
        private final BakedModel sledModel;
        @Nullable private final BakedModel headFuzeSledModel;
        @Nullable private final BakedModel baseFuzeSledModel;

        SledItemOverrides(ItemOverrides original, BakedModel sledModel,
                          @Nullable BakedModel headFuzeSledModel, @Nullable BakedModel baseFuzeSledModel) {
            this.original = original;
            this.sledModel = sledModel;
            this.headFuzeSledModel = headFuzeSledModel;
            this.baseFuzeSledModel = baseFuzeSledModel;
        }

        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
                                  @Nullable LivingEntity entity, int seed) {
            BakedModel resolved = original.resolve(model, stack, level, entity, seed);

            CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
            if (data == null) return resolved;
            CompoundTag beTag = data.copyTag();
            if (!beTag.getBoolean("Sled")) return resolved;

            if (beTag.contains("Fuze") && !ItemStack.parseOptional(
                    level == null ? net.minecraft.core.RegistryAccess.EMPTY : level.registryAccess(),
                    beTag.getCompound("Fuze")).isEmpty()) {
                if (resolved != model && resolved != null) {
                    float fuzeState = 0;
                    try {
                        var prop = net.minecraft.client.renderer.item.ItemProperties.getProperty(
                                stack, ResourceLocation.fromNamespaceAndPath("createbigcannons", "fuze_state"));
                        if (prop != null) fuzeState = prop.call(stack, level, entity, seed);
                    } catch (Exception ignored) {}

                    if (fuzeState >= 2.0f && baseFuzeSledModel != null) return baseFuzeSledModel;
                    if (fuzeState >= 1.0f && headFuzeSledModel != null) return headFuzeSledModel;
                }
                return sledModel;
            }

            return sledModel;
        }
    }
}
