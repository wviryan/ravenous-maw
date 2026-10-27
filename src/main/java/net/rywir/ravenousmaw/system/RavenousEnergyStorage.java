package net.rywir.ravenousmaw.system;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import org.jetbrains.annotations.NotNull;

public class RavenousEnergyStorage extends EnergyStorage {
    private static final int MAX_RECEIVE = 1000;
    private static final int MAX_EXTRACT = 1000;
    protected final ItemStack stack;

    public RavenousEnergyStorage(ItemStack stack) {
        // The stage is read from the item type, so the capacity follows the Maw through stage-ups.
        super(new StageHandler(stack).getStage().getEnergyCapacity(), MAX_RECEIVE, MAX_EXTRACT, 0);
        this.stack = stack;
        this.energy = stack.getOrDefault(DataComponentTypes.STORED_ENERGY, 0);
    }

    private void setEnergy(int amount) {
        stack.set(DataComponentTypes.STORED_ENERGY, amount);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        int energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            setEnergy(energy);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        int energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            setEnergy(energy);
        }
        return energyExtracted;
    }

    @Override
    public int getEnergyStored() {
        return stack.getOrDefault(DataComponentTypes.STORED_ENERGY, 0);
    }

    @Override
    public int getMaxEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    @Override
    public @NotNull Tag serializeNBT(HolderLookup.@NotNull Provider provider) {
        return IntTag.valueOf(this.getEnergyStored());
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull Tag nbt) {
        if (!(nbt instanceof IntTag intNbt))
            throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
        this.energy = intNbt.getAsInt();
    }
}
