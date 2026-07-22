package net.arsenalists.createenergycannons.neoforge.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.foundation.utility.CreateLang;
import net.arsenalists.createenergycannons.content.cooling.WaterTemp;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Appends the water's temperature to its displayed name wherever Create shows a fluid
 * (tank goggle tooltips, JEI, display links), but only once it's meaningfully hot, so
 * ordinary water reads normally and heated water shows its temperature wherever it appears.
 */
@Mixin(value = CreateLang.class, remap = false)
public class CreateLangMixin {

    @ModifyReturnValue(method = "fluidName", at = @At("RETURN"))
    private static LangBuilder createEnergyCannons$appendTemperature(LangBuilder original, FluidStack stack) {
        if (stack.getFluid() != Fluids.WATER) return original;
        int temp = WaterTemp.effective(stack);
        if (temp <= WaterTemp.AMBIENT) return original;
        return original.text(ChatFormatting.RED, " " + temp + "°");
    }
}
