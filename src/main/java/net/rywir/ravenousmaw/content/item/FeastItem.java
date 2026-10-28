package net.rywir.ravenousmaw.content.item;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class FeastItem extends Item {
    public FeastItem(Properties properties, Holder<MobEffect> effect, int nutrition, float saturation) {
        super(properties.food(
            new FoodProperties.Builder()
                .nutrition(nutrition)
                .alwaysEdible()
                .saturationModifier(-saturation) // It's disgusting
                .build(),
            // The effect always applies (probability 1.0), as the old food effect did
            Consumable.builder()
                .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(effect, 200, 1), 1.0f))
                .build())
        );
    }
}
