package net.arsenalists.createenergycannons.fabric.mixin;

import com.simibubi.create.foundation.utility.CreateLang;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Appends the water's temperature to its displayed name wherever Create shows a fluid - matching the
 * NeoForge CreateLangMixin, but reading the temperature from NBT (data components don't exist on 1.20.1).
 */
@Mixin(value = CreateLang.class, remap = false)
public class CreateLangMixin {

    @Inject(method = "fluidName", at = @At("RETURN"), cancellable = true)
    private static void createEnergyCannons$appendTemperature(FluidStack stack, CallbackInfoReturnable<LangBuilder> cir) {
        if (stack.getFluid() != Fluids.WATER) return;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("CECTemp")) return;
        long now = WaterTemp.currentTick;
        long heatedAt = tag.contains("CECHeatedAt") ? tag.getLong("CECHeatedAt") : now;
        int temp = WaterTemp.effective(tag.getInt("CECTemp"), heatedAt, now);
        if (temp <= WaterTemp.AMBIENT) return;
        cir.setReturnValue(cir.getReturnValue().text(ChatFormatting.RED, " " + temp + "°"));
    }
}
