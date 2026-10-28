package net.rywir.ravenousmaw.system;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import org.jspecify.annotations.Nullable;

/**
 * The Maw's battery: FE kept in the {@code STORED_ENERGY} data component of the stack.
 * Only exists while the Maw has Electric Mending (see {@code RavenousRegisterCapabilitiesEvent}).
 * <p>
 * Energy changes happen inside a {@link Transaction}, and a root transaction writes the result back to the real stack
 * when it commits. The static helpers open and commit that root transaction for callers that just want to read or
 * change the charge.
 */
public class RavenousEnergyStorage extends ItemAccessEnergyHandler {
    private static final int MAX_TRANSFER = 1000;

    public RavenousEnergyStorage(ItemAccess itemAccess) {
        // The stage is read from the item type, so the capacity follows the Maw through stage-ups.
        super(itemAccess, DataComponentTypes.STORED_ENERGY.get(),
            new StageHandler(itemAccess.getResource().toStack()).getStage().getEnergyCapacity(), MAX_TRANSFER);
    }

    /**
     * The battery of this Maw, or null when it has none (no Electric Mending, or not a Maw).
     */
    public static @Nullable EnergyHandler of(ItemStack stack) {
        if (stack.isEmpty()) return null;

        return stack.getCapability(Capabilities.Energy.ITEM, ItemAccess.forStack(stack));
    }

    /**
     * Adds up to {@code amount} FE and writes it to the stack.
     *
     * @return the amount actually stored
     */
    public static int insert(EnergyHandler energy, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            int inserted = energy.insert(amount, transaction);
            transaction.commit();
            return inserted;
        }
    }

    /**
     * Takes up to {@code amount} FE and writes it to the stack.
     *
     * @return the amount actually taken
     */
    public static int extract(EnergyHandler energy, int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = energy.extract(amount, transaction);
            transaction.commit();
            return extracted;
        }
    }
}
