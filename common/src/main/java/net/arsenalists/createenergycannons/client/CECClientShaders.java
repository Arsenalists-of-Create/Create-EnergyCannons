package net.arsenalists.createenergycannons.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ShaderInstance;

@Environment(EnvType.CLIENT)
public class CECClientShaders {
    public static ShaderInstance energyMuzzleParticleShader;
    public static ShaderInstance laserBeamShader;

    public static ShaderInstance getEnergyMuzzleParticleShader() {
        return energyMuzzleParticleShader;
    }

    public static ShaderInstance getLaserBeamShader() {
        return laserBeamShader;
    }
}
