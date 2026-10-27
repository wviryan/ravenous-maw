package net.rywir.ravenousmaw.registry;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.rywir.ravenousmaw.util.Constants;
import net.rywir.ravenousmaw.datagen.provider.RavenousBlockTagsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public enum Stages implements Tier {
    LATENT(Component.translatable("stage.ravenousmaw.latent"), 0, Items.ROTTEN_FLESH, RavenousBlockTagsProvider.INCORRECT_MAW, Constants.LATENT_TIER_USES, Constants.LATENT_TIER_SPEED, Constants.LATENT_TIER_ATTACK_DAMAGE_BONUS, Constants.LATENT_TIER_ENCHANTMENT_VALUE, EntityType.ENDER_DRAGON),
    ADVANCED(Component.translatable("stage.ravenousmaw.advanced"), 1, RavenousItems.PIGLIN_PIE.get(), RavenousBlockTagsProvider.INCORRECT_MAW, Constants.ADVANCED_TIER_USES, Constants.ADVANCED_TIER_SPEED, Constants.ADVANCED_TIER_ATTACK_DAMAGE_BONUS, Constants.ADVANCED_TIER_ENCHANTMENT_VALUE, EntityType.WITHER),
    NOBLE(Component.translatable("stage.ravenousmaw.noble"), 2, RavenousItems.CHORUS_CRACKER.get(), RavenousBlockTagsProvider.INCORRECT_MAW, Constants.NOBLE_TIER_USES, Constants.NOBLE_TIER_SPEED, Constants.NOBLE_TIER_ATTACK_DAMAGE_BONUS, Constants.NOBLE_TIER_ENCHANTMENT_VALUE, EntityType.WARDEN ),
    EXCELSIOR(Component.translatable("stage.ravenousmaw.excelsior"), 3, RavenousItems.SCULK_CROISSANT.get(), RavenousBlockTagsProvider.INCORRECT_MAW, Constants.EXCELSIOR_TIER_USES, Constants.EXCELSIOR_TIER_SPEED, Constants.EXCELSIOR_TIER_ATTACK_DAMAGE_BONUS, Constants.EXCELSIOR_TIER_ENCHANTMENT_VALUE, null );

    private final Component displayName;
    private final int id;
    private final Item feast;
    private final TagKey<Block> incorrectBlocksForDrops;
    private final int uses;
    private final float speed;
    private final float damage;
    private final int enchantmentValue;
    private @Nullable final EntityType<?> objective;
    private static final Map<Integer, Stages> BY_ID = new HashMap<>();
    private static final Set<EntityType<?>> OBJECTIVES = new HashSet<>();

    Stages(Component displayName, int id, Item feast, TagKey<Block> incorrectBlockForDrops, int uses, float speed, float damage, int enchantmentValue, EntityType<?> objective) {
        this.displayName = displayName;
        this.id = id;
        this.feast = feast;
        this.incorrectBlocksForDrops = incorrectBlockForDrops;
        this.uses = uses;
        this.speed = speed;
        this.damage = damage;
        this.enchantmentValue = enchantmentValue;
        this.objective = objective;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return this.damage;
    }

    @Override
    public @NotNull TagKey<Block> getIncorrectBlocksForDrops() {
        return this.incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return this.enchantmentValue;
    }

    @Override
    public @NotNull Ingredient getRepairIngredient() {
        // Maws are not repaired through the vanilla anvil: feasts repair them in FeedingRecipe.
        return Ingredient.EMPTY;
    }

    /** Electric Mending battery size in FE; grows with the stage. */
    public int getEnergyCapacity() {
        return switch (this) {
            case LATENT -> Constants.LATENT_TIER_ENERGY_CAPACITY;
            case ADVANCED -> Constants.ADVANCED_TIER_ENERGY_CAPACITY;
            case NOBLE -> Constants.NOBLE_TIER_ENERGY_CAPACITY;
            case EXCELSIOR -> Constants.EXCELSIOR_TIER_ENERGY_CAPACITY;
        };
    }

    public String getDisplayName() {
        return displayName.getString();
    }

    public int getId() {
        return id;
    }

    public Item getFeast() {
        return feast;
    }

    public @Nullable EntityType<?> getObjective() {
        return objective;
    }

    public static Set<EntityType<?>> getOBJECTIVES() {
        return OBJECTIVES;
    }

    public static Stages byId(int id) {
        return BY_ID.get(id);
    }

    static {
        for (Stages stage : values()) {
            BY_ID.put(stage.id, stage);
        }
    }

    static {
        for (Stages stage : values()) {
            if (stage.objective != null) {
                OBJECTIVES.add(stage.objective);
            }
        }
    }
}
