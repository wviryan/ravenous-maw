package net.rywir.ravenousmaw.util;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Configs {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    // COMBUSTIVE ENZYME
    public static final ModConfigSpec.DoubleValue COMBUSTIVE_ENZYME_ADVANCED_DAMAGE_BONUS = BUILDER
        .comment("Advanced Stage - Combustive Enzyme Bonus Damage Value")
        .defineInRange("advancedCombustive", Constants.COMBUSTIVE_ENZYME_ADVANCED_DAMAGE_BONUS, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue COMBUSTIVE_ENZYME_NOBLE_DAMAGE_BONUS = BUILDER
        .comment("Noble Stage - Combustive Enzyme Bonus Damage Value")
        .defineInRange("nobleCombustive", Constants.COMBUSTIVE_ENZYME_NOBLE_DAMAGE_BONUS, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue COMBUSTIVE_ENZYME_EXCELSIOR_DAMAGE_BONUS = BUILDER
        .comment("Excelsior Stage - Combustive Enzyme Bonus Damage Value")
        .defineInRange("excelsiorCombustive", Constants.COMBUSTIVE_ENZYME_EXCELSIOR_DAMAGE_BONUS, 0, Double.MAX_VALUE);


    // RESONANT RENDING
    public static final ModConfigSpec.DoubleValue RESONANT_RENDING_PERCENTAGE = BUILDER
        .comment("Resonant Rending - Max Health Percentage")
        .defineInRange("resonantPercentage", Constants.RESONANT_RENDING_PERCENTAGE, 0, Double.MAX_VALUE);


    // INSATIABLE VORACITY
    public static final ModConfigSpec.DoubleValue INSATIABLE_VORACITY_ADVANCED_DAMAGE_MULTIPLIER = BUILDER
        .comment("Advanced Stage - Insatiable Voracity Damage Multiplier")
        .defineInRange("advancedVoracity", Constants.INSATIABLE_VORACITY_ADVANCED_DAMAGE_MULTIPLIER, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue INSATIABLE_VORACITY_NOBLE_DAMAGE_MULTIPLIER = BUILDER
        .comment("Noble Stage - Insatiable Voracity Damage Multiplier")
        .defineInRange("nobleVoracity", Constants.INSATIABLE_VORACITY_NOBLE_DAMAGE_MULTIPLIER, 0, Double.MAX_VALUE);

    public static final ModConfigSpec.DoubleValue INSATIABLE_VORACITY_EXCELSIOR_DAMAGE_MULTIPLIER = BUILDER
        .comment("Excelsior Stage - Insatiable Voracity Damage Multiplier")
        .defineInRange("excelsiorVoracity", Constants.INSATIABLE_VORACITY_EXCELSIOR_DAMAGE_MULTIPLIER, 0, Double.MAX_VALUE);

    public static final ModConfigSpec SPEC = BUILDER.build();
}