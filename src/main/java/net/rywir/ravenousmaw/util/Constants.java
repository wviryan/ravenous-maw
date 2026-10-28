package net.rywir.ravenousmaw.util;

public class Constants {
    // LATENT
    public static final int LATENT_TIER_USES = 250;
    public static final float LATENT_TIER_SPEED = 6;
    public static final float LATENT_TIER_ATTACK_DAMAGE_BONUS = 2;
    public static final int LATENT_TIER_ENCHANTMENT_VALUE = 8;
    public static final int LATENT_TIER_ENERGY_CAPACITY = 5000;

    // ADVANCED
    public static final int ADVANCED_TIER_USES = 500;
    public static final float ADVANCED_TIER_SPEED = 8;
    public static final float ADVANCED_TIER_ATTACK_DAMAGE_BONUS = 3;
    public static final int ADVANCED_TIER_ENCHANTMENT_VALUE = 12;
    public static final int ADVANCED_TIER_ENERGY_CAPACITY = 10000;

    // NOBLE
    public static final int NOBLE_TIER_USES = 1000;
    public static final float NOBLE_TIER_SPEED = 10;
    public static final float NOBLE_TIER_ATTACK_DAMAGE_BONUS = 4;
    public static final int NOBLE_TIER_ENCHANTMENT_VALUE = 16;
    public static final int NOBLE_TIER_ENERGY_CAPACITY = 20000;

    // EXCELSIOR
    public static final int EXCELSIOR_TIER_USES = 2000;
    public static final float EXCELSIOR_TIER_SPEED = 12;
    public static final float EXCELSIOR_TIER_ATTACK_DAMAGE_BONUS = 5;
    public static final int EXCELSIOR_TIER_ENCHANTMENT_VALUE = 20;
    public static final int EXCELSIOR_TIER_ENERGY_CAPACITY = 40000;
    
    // MAW
    public static final float MAW_ATTACK_DAMAGE_MODIFIER = 3.0F;
    public static final float MAW_ATTACK_SPEED_MODIFIER = -2.4F;
    public static final float MAW_DURABILITY_PERCENTAGE_THRESHOLD = 0.05F;
    public static final int MAW_HIT_DURABILITY_COST = 1;
    /** Minimum ticks between two "below the durability threshold" warnings (sound + message) on a client. */
    public static final int MAW_DURABILITY_WARNING_COOLDOWN_TICKS = 80;

    // MUTATION CONSTANTS - TECTONIC BITE
    /** Minimum ticks between two "not enough durability" warnings for the same player. */
    public static final int TECTONIC_BITE_WARNING_COOLDOWN_TICKS = 80;

    // MAW TICK COUNTER (MawItem.inventoryTick)
    /** Ticks between two instabilities (9 minutes). */
    public static final int MAW_INSTABILITY_TICKS = 20 * 60 * 9;
    /** Ticks between two dialogue lines (3 minutes). */
    public static final int MAW_DIALOGUE_TICKS = 20 * 60 * 3;

    // ASSIMILATIONS
    /** Shortest and longest stay in a dimension (10 to 30 seconds) before it grants its assimilation. */
    public static final int DIMENSION_DEED_MIN_DELAY_TICKS = 20 * 10;
    public static final int DIMENSION_DEED_MAX_DELAY_TICKS = 20 * 30;

    // FEAST
    public static final int BASE_NUTRITION_MODIFIER = 4;
    public static final float BASE_SATURATION_MODIFIER = 2.0F;

    // MUTATION CONSTANTS - TENDRIL LASH
    public static final int TENDRIL_LASH_NOBLE_EXTRA_RANGE = 1;
    public static final int TENDRIL_LASH_EXCELSIOR_EXTRA_RANGE = 2;

    // MUTATION CONSTANTS - ELECTRIC MENDING
    public static final int ELECTRIC_MENDING_ATTACK_ENERGY_COST = 25;
    public static final float ELECTRIC_MENDING_LOW_ENERGY_DAMAGE_MULTIPLIER = 0.1F;
    /** Minimum ticks between two "battery drained" warnings (sound + message) for the same player. */
    public static final int ELECTRIC_MENDING_WARNING_COOLDOWN_TICKS = 80;

    // MUTATION CONSTANTS - SYMBIOTIC AID
    /** Minimum ticks between two "effect absorbed" notices (sound + message) for the same player. */
    public static final int SYMBIOTIC_AID_ABSORB_COOLDOWN_TICKS = 80;

    // STAGE-UP ANNOUNCEMENT
    public static final int STAGE_UP_PARTICLE_COUNT = 60;

    // MUTATION CONSTANTS - COMBUSTIVE ENZYME
    public static final float COMBUSTIVE_ENZYME_ADVANCED_DAMAGE_BONUS = 2.0F;
    public static final float COMBUSTIVE_ENZYME_NOBLE_DAMAGE_BONUS = 4.0F;
    public static final float COMBUSTIVE_ENZYME_EXCELSIOR_DAMAGE_BONUS = 6.0F;
    /** Fire Aspect Maws: ticks between the hit and the explosion. */
    public static final int COMBUSTIVE_ENZYME_FUSE_TICKS = 12;

    // MUTATION CONSTANTS - RESONANT RENDING
    public static final float RESONANT_RENDING_PERCENTAGE = 0.06F;

    // MUTATION CONSTANTS - INSATIABLE VORACITY
    public static final float INSATIABLE_VORACITY_ADVANCED_DAMAGE_MULTIPLIER = 0.01F;
    public static final float INSATIABLE_VORACITY_NOBLE_DAMAGE_MULTIPLIER = 0.02F;
    public static final float INSATIABLE_VORACITY_EXCELSIOR_DAMAGE_MULTIPLIER = 0.03F;
    // Ceiling of the kill bonus per stage (deliberately generous)
    public static final float INSATIABLE_VORACITY_ADVANCED_MAX_DAMAGE = 15.0F;
    public static final float INSATIABLE_VORACITY_NOBLE_MAX_DAMAGE = 40.0F;
    public static final float INSATIABLE_VORACITY_EXCELSIOR_MAX_DAMAGE = 100.0F;

    // REPAIR AMOUNTS
    public static final int LATENT_ROTTEN_FLESH = 125;

    public static final int ADVANCED_ROTTEN_FLESH = 25;
    public static final int ADVANCED_PIGLIN_PIE = 250;

    public static final int NOBLE_ROTTEN_FLESH = 5;
    public static final int NOBLE_PIGLIN_PIE = 50;
    public static final int NOBLE_CHORUS_CRACKER = 500;

    public static final int EXCELSIOR_ROTTEN_FLESH = 1 * 5; // * 5 EXCELSIOR STAGE BOOST
    public static final int EXCELSIOR_PIGLIN_PIE = 10 * 5;
    public static final int EXCELSIOR_CHORUS_CRACKER = 100 * 5;
    public static final int EXCELSIOR_SCULK_CROISSANT = 1000;
}