package net.rywir.ravenousmaw.content.decorator;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.rywir.ravenousmaw.system.RavenousEnergyStorage;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.MutationHandler;
import org.jetbrains.annotations.NotNull;

public class EnergyBarDecorator implements IItemDecorator {
    @Override
    public boolean render(@NotNull GuiGraphicsExtractor graphics, @NotNull Font font, @NotNull ItemStack stack, int x, int y) {
        MutationHandler handler = new MutationHandler(stack);

        boolean hasElectricMending = handler.has(Mutations.ELECTRIC_MENDING);

        if (!hasElectricMending) return false;

        var energy = RavenousEnergyStorage.of(stack);

        if (energy == null) return false;

        int width = Math.round(13f * energy.getAmountAsInt() / energy.getCapacityAsInt());

        int left = x + 2;

        boolean hasUndyingFlesh = handler.has(Mutations.UNDYING_FLESH);

        boolean isEnergetic = energy.getAmountAsInt() > 0;

        int top = hasUndyingFlesh || isEnergetic ? y + 13 : y + 11;

        graphics.fill(left, top, left + 13, top + 2, 0xFF000000);
        graphics.fill(left, top, left + width, top + 1, 0xFFFFFF00);

        return false;
    }
}