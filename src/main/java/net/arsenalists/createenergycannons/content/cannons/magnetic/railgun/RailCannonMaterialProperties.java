package net.arsenalists.createenergycannons.content.cannons.magnetic.railgun;

public record RailCannonMaterialProperties(double minimumVelocityPerBarrel, float weight, int maxSafePropellantStress,
                                           rbasamoyai.createbigcannons.cannons.big_cannons.material.BigCannonMaterialProperties.FailureMode failureMode,
                                           boolean connectsInSurvival, boolean isWeldable, int weldDamage,
                                           int weldStressPenalty, float minimumSpread, float spreadReductionPerBarrel) {

}
