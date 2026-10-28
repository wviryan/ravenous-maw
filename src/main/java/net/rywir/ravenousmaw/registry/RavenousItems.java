package net.rywir.ravenousmaw.registry;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.item.CodexItem;
import net.rywir.ravenousmaw.content.item.FeastItem;
import net.rywir.ravenousmaw.content.item.MawItem;
import net.rywir.ravenousmaw.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class RavenousItems {
    public static final DeferredRegister.Items FEASTS = DeferredRegister.createItems(RavenousMaw.MOD_ID);
    public static final DeferredRegister.Items MAWS = DeferredRegister.createItems(RavenousMaw.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(RavenousMaw.MOD_ID);


    // FEASTS
    public static final DeferredItem<Item> PIGLIN_PIE = FEASTS.registerItem("piglin_pie",
        props -> new FeastItem(props, MobEffects.POISON, Constants.BASE_NUTRITION_MODIFIER, Constants.BASE_SATURATION_MODIFIER));

    public static final DeferredItem<Item> CHORUS_CRACKER = FEASTS.registerItem("chorus_cracker",
        props -> new FeastItem(props, MobEffects.LEVITATION, Constants.BASE_NUTRITION_MODIFIER * 2, Constants.BASE_SATURATION_MODIFIER * 2));

    public static final DeferredItem<Item> SCULK_CROISSANT = FEASTS.registerItem("sculk_croissant",
        props -> new FeastItem(props, MobEffects.DARKNESS, Constants.BASE_NUTRITION_MODIFIER * 4, Constants.BASE_SATURATION_MODIFIER * 4));


    // MAWS
    public static final Map<Stages, Item> MAW_BY_STAGE = new HashMap<>();

    public static final DeferredItem<Item> RAVENOUS_MAW_LATENT = MAWS.registerItem("ravenous_maw_latent",
        MawItem::new, () -> MawItem.createProperties(Stages.LATENT));

    public static final DeferredItem<Item> RAVENOUS_MAW_ADVANCED = MAWS.registerItem("ravenous_maw_advanced",
        MawItem::new, () -> MawItem.createProperties(Stages.ADVANCED));

    public static final DeferredItem<Item> RAVENOUS_MAW_NOBLE = MAWS.registerItem("ravenous_maw_noble",
        MawItem::new, () -> MawItem.createProperties(Stages.NOBLE));

    public static final DeferredItem<Item> RAVENOUS_MAW_EXCELSIOR = MAWS.registerItem("ravenous_maw_excelsior",
        MawItem::new, () -> MawItem.createProperties(Stages.EXCELSIOR));


    // OTHER ITEMS
    public static final DeferredItem<Item> CONTAMINATION_CORE = ITEMS.registerSimpleItem("contamination_core");

    public static final DeferredItem<Item> MUTATION_CODEX = ITEMS.registerItem("mutation_codex", CodexItem::new);


    public static void registerFeasts(IEventBus bus) {
        FEASTS.register(bus);
    }

    public static void registerMaws(IEventBus bus) {
        MAWS.register(bus);
    }

    public static void registerItems(IEventBus bus) {
        ITEMS.register(bus);
    }
}
