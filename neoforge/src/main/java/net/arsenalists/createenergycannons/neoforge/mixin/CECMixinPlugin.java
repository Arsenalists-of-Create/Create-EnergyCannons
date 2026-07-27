package net.arsenalists.createenergycannons.neoforge.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.util.List;
import java.util.Set;

/**
 * CBC 5.11.4 started overriding saveAdditional/loadAdditional on the big cannon projectile.
 * Declaring them from a mixin would replace CBC's copy and silently drop the shell's tracer,
 * so read the target up front and pick the hook that fits whichever CBC is installed.
 */
public class CECMixinPlugin implements IMixinConfigPlugin {

    private static final String PROJECTILE_BE =
        "rbasamoyai/createbigcannons/munitions/big_cannon/BigCannonProjectileBlockEntity";

    private Boolean cbcSavesItsOwnNbt;

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("BigCannonProjectileNbtInjectMixin")) return cbcSavesItsOwnNbt();
        if (mixinClassName.endsWith("BigCannonProjectileNbtOverrideMixin")) return !cbcSavesItsOwnNbt();
        return true;
    }

    private boolean cbcSavesItsOwnNbt() {
        if (cbcSavesItsOwnNbt == null) {
            boolean declared = false;
            try {
                ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(PROJECTILE_BE);
                for (MethodNode method : node.methods) {
                    if (method.name.equals("saveAdditional")) {
                        declared = true;
                        break;
                    }
                }
            } catch (Exception e) {
                // Reading it failed, so assume the shape we ship against and let Mixin report the rest.
                declared = true;
            }
            cbcSavesItsOwnNbt = declared;
        }
        return cbcSavesItsOwnNbt;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
