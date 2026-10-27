package net.rywir.ravenousmaw.content.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class RavenousCloneEvent {
    /** List of Maws saved on death by Symbiotic Aid (written in RavenousLivingDropsEvent). */
    public static final String SAVED_MAWS_KEY = "SavedMaws";
    /** Old single-Maw key, only read so Maws saved by earlier versions are not lost. */
    private static final String LEGACY_SAVED_MAW_KEY = "SavedMaw";

    @SubscribeEvent
    public static void onCloneEvent(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player dead = event.getOriginal();
        Player reborn = event.getEntity();

        CompoundTag inheritance = dead.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);

        ListTag savedMaws = inheritance.getList(SAVED_MAWS_KEY, Tag.TAG_COMPOUND);

        if (inheritance.contains(LEGACY_SAVED_MAW_KEY)) {
            savedMaws.add(inheritance.getCompound(LEGACY_SAVED_MAW_KEY));
        }

        for (int i = 0; i < savedMaws.size(); i++) {
            ItemStack restoredMaw = ItemStack.parseOptional(reborn.registryAccess(), savedMaws.getCompound(i));

            if (restoredMaw.isEmpty()) continue;

            // Goes into the inventory, or drops at the player's feet when full, so no Maw is lost
            reborn.getInventory().placeItemBackInInventory(restoredMaw);
        }

        // Clear both tags: the new player may already hold a merged copy of the persisted data,
        // which would restore the same Maws again on the next death
        inheritance.remove(SAVED_MAWS_KEY);
        inheritance.remove(LEGACY_SAVED_MAW_KEY);

        CompoundTag rebornPersisted = reborn.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        rebornPersisted.remove(SAVED_MAWS_KEY);
        rebornPersisted.remove(LEGACY_SAVED_MAW_KEY);
    }
}
