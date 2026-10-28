package net.rywir.ravenousmaw.datagen.provider;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rywir.ravenousmaw.registry.Assimilations;
import net.rywir.ravenousmaw.registry.RavenousItems;
import net.rywir.ravenousmaw.system.AssimilationHandler;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Advancement tab of the mod: a root earned by obtaining a Maw, one advancement per assimilation and a
 * challenge for earning all of them. The game lays positions out from the parent tree (an advancement has a single
 * parent), so the assimilations form a chain following the stage progression, ending in the challenge. The assimilation ones use impossible criteria, awarded by
 * {@link AssimilationHandler#grant} at the moment the assimilation is granted.
 */
public class RavenousAdvancementProvider extends AdvancementProvider {
    public RavenousAdvancementProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, List.of(new RavenousAdvancements()));
    }

    private static class RavenousAdvancements implements AdvancementSubProvider {
        private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/advancements/backgrounds/adventure.png");

        @Override
        public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver) {
            AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                    new ItemStack(RavenousItems.RAVENOUS_MAW_LATENT.get()),
                    Component.translatable("advancement.ravenousmaw.root.title"),
                    Component.translatable("advancement.ravenousmaw.root.description"),
                    BACKGROUND, AdvancementType.TASK, true, true, false)
                .addCriterion("has_maw", InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(RavenousItemTagsProvider.MAW)))
                .build(AssimilationHandler.ROOT_ADVANCEMENT);
            saver.accept(root);

            Map<Assimilations, AdvancementHolder> holders = new EnumMap<>(Assimilations.class);

            for (Assimilations assimilation : Assimilations.values()) {
                Assimilations parent = parentOf(assimilation);

                AdvancementHolder holder = Advancement.Builder.advancement()
                    .parent(parent == null ? root : holders.get(parent))
                    .display(
                        new ItemStack(icon(assimilation)),
                        Component.translatable("advancement.ravenousmaw.assimilation." + assimilation.getKey() + ".title"),
                        Component.translatable("advancement.ravenousmaw.assimilation." + assimilation.getKey() + ".description"),
                        null, assimilation == Assimilations.ELDRITCH_ANTENNA ? AdvancementType.GOAL : AdvancementType.TASK, true, true, false)
                    .addCriterion(AssimilationHandler.ASSIMILATED_CRITERION, CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()))
                    .build(AssimilationHandler.advancementId(assimilation));

                holders.put(assimilation, holder);
                saver.accept(holder);
            }

            Advancement.Builder all = Advancement.Builder.advancement()
                .parent(holders.get(Assimilations.ELDRITCH_ANTENNA))
                .display(
                    new ItemStack(RavenousItems.RAVENOUS_MAW_EXCELSIOR.get()),
                    Component.translatable("advancement.ravenousmaw.assimilation_all.title"),
                    Component.translatable("advancement.ravenousmaw.assimilation_all.description"),
                    null, AdvancementType.CHALLENGE, true, true, false);

            for (Assimilations assimilation : Assimilations.values()) {
                all.addCriterion(assimilation.getKey(), CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance()));
            }

            saver.accept(all.build(AssimilationHandler.ALL_ADVANCEMENT));
        }

        /** The assimilation an assimilation's advancement hangs from, or null for the root. Parents come earlier in the enum. */
        private static Assimilations parentOf(Assimilations assimilation) {
            return switch (assimilation) {
                case GILDED_PHEROMONES    -> null;
                case NICTITATING_MEMBRANE -> Assimilations.GILDED_PHEROMONES;
                case AQUANAUT_COALESCENCE -> Assimilations.GILDED_PHEROMONES;
                case OXYTOCINERGIC_ESCA   -> Assimilations.NICTITATING_MEMBRANE;
                case ELDRITCH_ANTENNA     -> Assimilations.OXYTOCINERGIC_ESCA;
            };
        }

        private static Item icon(Assimilations assimilation) {
            return switch (assimilation) {
                case GILDED_PHEROMONES    -> Items.GOLD_INGOT;
                case NICTITATING_MEMBRANE -> Items.ENDER_EYE;
                case AQUANAUT_COALESCENCE -> Items.HEART_OF_THE_SEA;
                case OXYTOCINERGIC_ESCA   -> Items.EMERALD;
                case ELDRITCH_ANTENNA     -> Items.ECHO_SHARD;
            };
        }
    }
}
