package net.rywir.ravenousmaw.content.recipe.feeding;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousItems;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.util.Constants;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FeedingRecipe extends CustomRecipe {
    // For better ordering
    public static final Map<Stages, Map<ItemLike, Integer>> REPAIR_MAP = Map.of(
        Stages.LATENT, Stream.of(
            Map.entry(Items.ROTTEN_FLESH.asItem(), Constants.LATENT_ROTTEN_FLESH)
        ).collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (oldVal, newVal) -> oldVal,
            LinkedHashMap::new
        )),

        Stages.ADVANCED, Stream.of(
            Map.entry(RavenousItems.PIGLIN_PIE.asItem(), Constants.ADVANCED_PIGLIN_PIE),
            Map.entry(Items.ROTTEN_FLESH.asItem(), Constants.ADVANCED_ROTTEN_FLESH)
        ).collect(Collectors.toMap(
            Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new
        )),

        Stages.NOBLE, Stream.of(
            Map.entry(RavenousItems.CHORUS_CRACKER.asItem(), Constants.NOBLE_CHORUS_CRACKER),
            Map.entry(RavenousItems.PIGLIN_PIE.asItem(), Constants.NOBLE_PIGLIN_PIE),
            Map.entry(Items.ROTTEN_FLESH.asItem(), Constants.NOBLE_ROTTEN_FLESH)
        ).collect(Collectors.toMap(
            Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new
        )),

        Stages.EXCELSIOR, Stream.of(
            Map.entry(RavenousItems.SCULK_CROISSANT.asItem(), Constants.EXCELSIOR_SCULK_CROISSANT),
            Map.entry(RavenousItems.CHORUS_CRACKER.asItem(), Constants.EXCELSIOR_CHORUS_CRACKER),
            Map.entry(RavenousItems.PIGLIN_PIE.asItem(), Constants.EXCELSIOR_PIGLIN_PIE),
            Map.entry(Items.ROTTEN_FLESH.asItem(), Constants.EXCELSIOR_ROTTEN_FLESH)
        ).collect(Collectors.toMap(
            Map.Entry::getKey, Map.Entry::getValue, (oldVal, newVal) -> oldVal, LinkedHashMap::new
        ))
    );

    public static final MapCodec<FeedingRecipe> MAP_CODEC = MapCodec.unit(FeedingRecipe::new);
    // Not StreamCodec.unit: it demands the very same instance when encoding, but every loaded recipe is a new one.
    public static final StreamCodec<RegistryFriendlyByteBuf, FeedingRecipe> STREAM_CODEC = StreamCodec.of((buf, recipe) -> {}, buf -> new FeedingRecipe());
    public static final RecipeSerializer<FeedingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack maw = ItemStack.EMPTY;
        ItemStack feast = ItemStack.EMPTY;

        int itemsFound = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.isEmpty()) continue;

            itemsFound++;

            if (stack.is(RavenousItemTagsProvider.MAW)) {
                if (maw.isEmpty()) {
                    maw = stack;
                } else {
                    return false;
                }
            }

            else if (stack.is(RavenousItemTagsProvider.FEAST)) {
                if (feast.isEmpty()) {
                    feast = stack;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (itemsFound != 2 || maw.isEmpty() || feast.isEmpty()) return false;

        if (!maw.isDamaged()) return false;

        StageHandler stageHandler = new StageHandler(maw);
        Stages stage = stageHandler.getStage();
        Item feastItem = feast.getItem();

        if (stage == null || !REPAIR_MAP.containsKey(stage)) return false;

        boolean isApplicable = REPAIR_MAP.get(stage).containsKey(feastItem);

        MutationHandler mutationHandler = new MutationHandler(maw);
        boolean hasUndyingFlesh = mutationHandler.has(Mutations.UNDYING_FLESH);

        return isApplicable && !hasUndyingFlesh;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack maw = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (stack.is(RavenousItemTagsProvider.MAW)) {
                maw = stack.copy();
                break;
            }
        }

        Item feastItem = ItemStack.EMPTY.getItem();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(RavenousItemTagsProvider.FEAST)) {
                feastItem = stack.getItem();
                break;
            }
        }

        StageHandler handler = new StageHandler(maw);

        int amount = REPAIR_MAP.get(handler.getStage()).get(feastItem);

        int currentDamage = maw.getDamageValue();
        int newDamage = Math.max(0, currentDamage - amount);

        maw.setDamageValue(newDamage);

        return maw;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<FeedingRecipe> getSerializer() {
        return SERIALIZER;
    }
}
