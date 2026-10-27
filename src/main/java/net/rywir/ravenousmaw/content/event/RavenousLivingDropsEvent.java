package net.rywir.ravenousmaw.content.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.system.EnchantmentHandler;
import net.rywir.ravenousmaw.system.MutationHandler;

import java.util.ArrayList;
import java.util.List;

public class RavenousLivingDropsEvent {
    @SubscribeEvent
    public static void onLivingDropsEvent(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();

        if (level.isClientSide()) {
            return;
        }

        if (entity instanceof Player player) {
            List<ItemEntity> toSave = new ArrayList<>();

            for (ItemEntity itemEntity : event.getDrops()) {
                ItemStack stack = itemEntity.getItem();

                boolean isMaw = stack.is(RavenousItemTagsProvider.MAW);
                if (!isMaw) continue;

                MutationHandler handler = new MutationHandler(stack);

                boolean hasSymbioticAid = handler.has(Mutations.SYMBIOTIC_AID);
                if (!hasSymbioticAid) continue;

                toSave.add(itemEntity);
            }

            if (!toSave.isEmpty()) {
                CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                // Every saved Maw is appended, so several Symbiotic Aid Maws never overwrite each other
                ListTag savedMaws = persisted.getList(RavenousCloneEvent.SAVED_MAWS_KEY, Tag.TAG_COMPOUND);

                for (ItemEntity saved : toSave) {
                    event.getDrops().remove(saved);
                    savedMaws.add(saved.getItem().save(player.registryAccess()));
                }

                persisted.put(RavenousCloneEvent.SAVED_MAWS_KEY, savedMaws);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
            }
        }

        // Loot rules below (Reckless Devourer, Looting III) must never touch a player's death inventory
        if (entity instanceof Player) {
            return;
        }

        ItemStack stack = event.getSource().getWeaponItem();

        if (stack == null || !stack.is(RavenousItemTagsProvider.MAW)) {
            return;
        }

        MutationHandler mutationHandler = new MutationHandler(stack);

        boolean hasArcaneHypertrophy = mutationHandler.has(Mutations.ARCANE_HYPERTROPHY);
        boolean hasAdaptiveShift = mutationHandler.has(Mutations.ADAPTIVE_SHIFT);

        if (!hasAdaptiveShift && !hasArcaneHypertrophy) {
            return;
        }

        if (hasAdaptiveShift) {
            // Reckless Devourer ON (1) = double damage in exchange for no loot
            int isRecklessActive = mutationHandler.getConfigVal(Mutations.Parameters.RECKLESS_DEVOURER);

            if (isRecklessActive == 1) {
                event.getDrops().clear();
                return;
            }
        }

        if (hasArcaneHypertrophy) {
            EnchantmentHandler enchantmentHandler = new EnchantmentHandler(stack);
            boolean hasLootingMaxedOut = enchantmentHandler.hasMaxedOut(Enchantments.LOOTING, level);

            if (!hasLootingMaxedOut) {
                return;
            }

            List<ItemEntity> originalDrops = new ArrayList<>(event.getDrops());

            for (ItemEntity item : originalDrops) {
                ItemEntity duplicate = new ItemEntity(
                    level,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    item.getItem().copy()
                );
                level.addFreshEntity(duplicate);
            }
        }
    }
}
