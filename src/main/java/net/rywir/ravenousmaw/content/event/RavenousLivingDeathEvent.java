package net.rywir.ravenousmaw.content.event;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.rywir.ravenousmaw.datagen.provider.RavenousItemTagsProvider;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;

public class RavenousLivingDeathEvent {
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityType<?> type = event.getEntity().getType();

        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();

        if (!stack.is(RavenousItemTagsProvider.MAW)) {
            return;
        }

        MutationHandler mutationHandler = new MutationHandler(stack);
        Mutations voracity = Mutations.INSATIABLE_VORACITY;

        boolean hasVoracity = mutationHandler.has(voracity);

        if (hasVoracity) {
            mutationHandler.increaseKill();
        }

        if (!Stages.getOBJECTIVES().contains(type)) {
            return;
        }

        StageHandler handler = new StageHandler(stack);
        Stages stage = handler.getStage();

        if (!(stage.getObjective() == type)) {
            return;
        }

        ItemStack newMaw = handler.stageUp(stage.getId(), stack, player.level().registryAccess());

        if (!(player.getMainHandItem().is(RavenousItemTagsProvider.MAW))) {
            return;
        }

        player.setItemSlot(EquipmentSlot.MAINHAND, newMaw);
    }
}
