package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.registry.RavenousItems;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class RavenousItemTagsProvider extends ItemTagsProvider {
    public static final TagKey<Item> FEAST = ItemTags.create(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "feast"));
    public static final TagKey<Item> PAXEL = ItemTags.create(Identifier.fromNamespaceAndPath("c", "paxel"));
    public static final TagKey<Item> MUTAGEN = ItemTags.create(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "mutagen"));
    public static final TagKey<Item> MAW = ItemTags.create(Identifier.fromNamespaceAndPath(RavenousMaw.MOD_ID, "maw"));

    public RavenousItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, RavenousMaw.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        this.tag(Tags.Items.TOOLS)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(Tags.Items.MELEE_WEAPON_TOOLS)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(ItemTags.SHARP_WEAPON_ENCHANTABLE)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(ItemTags.BREAKS_DECORATED_POTS)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(MAW)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(MUTAGEN)
            .add(Items.STICKY_PISTON)
            .add(Items.MAGMA_CREAM)
            .add(Items.ENCHANTED_GOLDEN_APPLE)
            .add(Items.TOTEM_OF_UNDYING)
            .add(Items.PUFFERFISH)
            .add(Items.ANGLER_POTTERY_SHERD)
            .add(Items.GHAST_TEAR)
            .add(Items.TORCHFLOWER_SEEDS)
            .add(Items.DRAGON_BREATH)
            .add(Items.NETHER_STAR);

        this.tag(FEAST)
            .add(Items.ROTTEN_FLESH)
            .add(RavenousItems.PIGLIN_PIE.get())
            .add(RavenousItems.CHORUS_CRACKER.get())
            .add(RavenousItems.SCULK_CROISSANT.get());

        // 26.1 split the old sword tag: melee enchantments (fire aspect, knockback...) and sweeping edge.
        this.tag(ItemTags.MELEE_WEAPON_ENCHANTABLE)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());
        this.tag(ItemTags.SWEEPING_ENCHANTABLE)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(ItemTags.MINING_ENCHANTABLE)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(ItemTags.MINING_LOOT_ENCHANTABLE)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(ItemTags.DURABILITY_ENCHANTABLE)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(ItemTags.WEAPON_ENCHANTABLE)
            .add(RavenousItems.RAVENOUS_MAW_LATENT.get())
            .add(RavenousItems.RAVENOUS_MAW_ADVANCED.get())
            .add(RavenousItems.RAVENOUS_MAW_NOBLE.get())
            .add(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get());

        this.tag(PAXEL);
    }
}
