package net.rywir.ravenousmaw.content.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public class FeastItem extends Item {
    public FeastItem(Holder<MobEffect> effect, int nutrition, float saturation) {
        super(new Item.Properties().food(new FoodProperties.Builder()
            .nutrition(nutrition)
            .alwaysEdible()
            .saturationModifier(-saturation) // It's disgusting
            .effect(() -> new MobEffectInstance(effect, 200, 1), 1.0f)
            .build())
        );
    }
}
