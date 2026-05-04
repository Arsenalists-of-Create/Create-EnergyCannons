package net.arsenalists.createenergycannons.neoforge.loot;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.arsenalists.createenergycannons.content.cannons.magnetic.sled.IMagneticSled;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Injects the "Sled" tag into the BLOCK_ENTITY_DATA component of dropped shells so
 * magnetic sleds persist across pickup/place — same intent as the Forge variant.
 */
public class SledLootModifier extends LootModifier {

    public static final Supplier<MapCodec<SledLootModifier>> CODEC = Suppliers.memoize(() ->
            RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, SledLootModifier::new)));

    protected SledLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockEntity be = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
        if (be instanceof IMagneticSled sled && sled.isSled()) {
            for (ItemStack stack : generatedLoot) {
                if (!stack.isEmpty()) {
                    CustomData existing = stack.get(DataComponents.BLOCK_ENTITY_DATA);
                    CompoundTag tag = existing == null ? new CompoundTag() : existing.copyTag();
                    tag.putBoolean("Sled", true);
                    stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
