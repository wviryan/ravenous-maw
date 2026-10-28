package net.rywir.ravenousmaw.content.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.system.AssimilationHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Gilded Pheromones: improves the piglin barter result for a player who earned the assimilation.
 * Vanilla has no barter event, so this global loot modifier is attached (by datagen) to the barter loot table only.
 * The barterer is the player the piglin itself would throw the loot at: its nearest visible player.
 */
public class PiglinBarterModifier extends LootModifier {
    public static final MapCodec<PiglinBarterModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
        codecStart(instance).apply(instance, PiglinBarterModifier::new)
    );

    /** Chance that a barter pays double. */
    private static final float DOUBLE_CHANCE = 0.25F;
    /** Chance that a barter also pays a wither skeleton skull. */
    private static final float SKULL_CHANCE = 0.10F;

    public PiglinBarterModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof Piglin piglin)) return generatedLoot;

        Player barterer = piglin.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).orElse(null);
        if (barterer == null || !AssimilationHandler.isActive(barterer, Assimilations.GILDED_PHEROMONES)) return generatedLoot;

        if (context.getRandom().nextFloat() < DOUBLE_CHANCE) {
            for (ItemStack stack : generatedLoot) {
                stack.setCount(Math.min(stack.getCount() * 2, stack.getMaxStackSize()));
            }
        }

        if (context.getRandom().nextFloat() < SKULL_CHANCE) {
            generatedLoot.add(new ItemStack(Items.WITHER_SKELETON_SKULL));
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
