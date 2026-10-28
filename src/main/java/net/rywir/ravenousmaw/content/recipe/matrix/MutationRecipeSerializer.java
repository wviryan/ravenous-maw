package net.rywir.ravenousmaw.content.recipe.matrix;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

/** Codecs for {@link MutationRecipe}; in 26.1 a {@link RecipeSerializer} is a record of the two codecs. */
public final class MutationRecipeSerializer {
    private static final MapCodec<MutationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        Ingredient.CODEC.fieldOf("base").forGetter(MutationRecipe::getBase),
        Ingredient.CODEC.fieldOf("feast").forGetter(MutationRecipe::getFeast),
        Ingredient.CODEC.fieldOf("mutagen").forGetter(MutationRecipe::getMutagen)
    ).apply(inst, MutationRecipe::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, MutationRecipe::getBase,
        Ingredient.CONTENTS_STREAM_CODEC, MutationRecipe::getFeast,
        Ingredient.CONTENTS_STREAM_CODEC, MutationRecipe::getMutagen,
        MutationRecipe::new
    );

    public static final RecipeSerializer<MutationRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private MutationRecipeSerializer() {}
}
