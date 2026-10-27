package net.rywir.ravenousmaw.content.item;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.rywir.ravenousmaw.content.attachment.MawTickData;
import net.rywir.ravenousmaw.content.component.MutationComponent;
import net.rywir.ravenousmaw.content.gui.menu.ConfigurationMenu;
import net.rywir.ravenousmaw.registry.DataComponentTypes;
import net.rywir.ravenousmaw.registry.Mutations;
import net.rywir.ravenousmaw.registry.RavenousAttachments;
import net.rywir.ravenousmaw.registry.Stages;
import net.rywir.ravenousmaw.datagen.provider.RavenousBlockTagsProvider;
import net.rywir.ravenousmaw.system.AbilityDispatcher;
import net.rywir.ravenousmaw.system.MutationHandler;
import net.rywir.ravenousmaw.system.StageHandler;
import net.rywir.ravenousmaw.util.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MawItem extends Item {
    private static final int USE_DURATION               = 72000;
    private static final int FULL_CHARGE_TICKS          = 20;
    private static final int ENERGY_COST_PER_DURABIITY  = 50;
    private static final int MAX_TICKS                  = 20 * 60 * 9;
    private static final int DIALOGUE_FREQUENCY         = 20 * 60 * 3;

    private static final Component DURABILITY_WARNING = Component.translatable("ravenousmaw.maw_threshold_message");

    public MawItem(Stages stage) {
        super(createProperties(stage));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        Tool tool = (Tool)stack.get(DataComponents.TOOL);

        float divider = 1;

        MutationHandler mutationHandler = new MutationHandler(stack);
        boolean hasTectonicBite = mutationHandler.has(Mutations.TECTONIC_BITE);

        // Tectonic Bite instability
        if (hasTectonicBite) {
            int range = mutationHandler.getConfigVal(Mutations.Parameters.TECTONIC_AREA);

            StageHandler stageHandler = new StageHandler(stack);
            Stages stage = stageHandler.getStage();

            boolean isExcelsior = stage == Stages.EXCELSIOR;

            if (!isExcelsior && range != 1) {
                divider = range;
            }
        }

        boolean hasAdaptiveShift = mutationHandler.has(Mutations.ADAPTIVE_SHIFT);
        if (hasAdaptiveShift) {
            int haste = mutationHandler.getConfigVal(Mutations.Parameters.EXCAVATION_HASTE);

            double multiplier = haste / 10.0;
            divider /= multiplier;
        }

        return tool.getMiningSpeed(state) / divider;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        StageHandler stageHandler = new StageHandler(stack);
        String stageString = Component.translatable("maw.ravenousmaw.stage_string").getString();
        tooltipComponents.add(Component.literal(stageString + ": " + stageHandler.getStage().getDisplayName()));

        MutationHandler handler = new MutationHandler(stack);
        List<String> displayNames = handler.getDisplayNames();

        boolean hasElectricMending = handler.has(Mutations.ELECTRIC_MENDING);

        if (hasElectricMending) {
            String energyString = Component.translatable("maw.ravenousmaw.energy_string").getString();

            var energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);

            Component tooltip = Component.literal(energyString + ": " + energy.getEnergyStored() + "/" + energy.getMaxEnergyStored());
            tooltipComponents.add(tooltip);
        }

        if (Screen.hasShiftDown()) {
            String mutationString = Component.translatable("maw.ravenousmaw.mutation_string").getString();
            tooltipComponents.add(Component.literal(mutationString + ":").withStyle(ChatFormatting.YELLOW));
            displayNames.forEach(name -> tooltipComponents.add(Component.literal("・" + name).withStyle(ChatFormatting.GRAY))
            );
        } else {
            String mutationString = Component.translatable("maw.ravenousmaw.mutation_string").getString();
            tooltipComponents.add(Component.literal(mutationString + ": " + displayNames.size()).withStyle(ChatFormatting.YELLOW));
            tooltipComponents.add(Component.translatable("tooltip.ravenousmaw.shift").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        AbilityDispatcher dispatcher = new AbilityDispatcher();
        dispatcher.onAttack(stack, target, target.level());
        return true;
    }

    @Override
    public float getAttackDamageBonus(Entity target, float damage, DamageSource damageSource) {
        ItemStack stack = damageSource.getWeaponItem();

        if (stack == null) {
            return 0F;
        }

        Stages stage = new StageHandler(stack).getStage();

        if (stage == null) {
            return 0F;
        }

        Level level = target.level();

        if (level.isClientSide()) {
            return 0F;
        }

        // Dragon hitboxes are separate entities; the bonus applies to the dragon itself (as in IrisBulletEntity).
        if (target instanceof EnderDragonPart part) {
            target = part.parentMob;
        }

        // Boats, minecarts, item frames, end crystals, etc. take no mutation bonus.
        if (!(target instanceof LivingEntity livingTarget)) {
            return 0F;
        }

        MutationHandler mutationHandler = new MutationHandler(stack);
        AbilityDispatcher dispatcher = new AbilityDispatcher();

        return dispatcher.getAttackDamageBonus(mutationHandler, stack, livingTarget, level, stage);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!canSurvive(stack, level, player)) return InteractionResultHolder.fail(stack);

        boolean isClientSide = level.isClientSide();

        if (isClientSide) {
            return InteractionResultHolder.fail(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        boolean isShiftDown = player.isShiftKeyDown();

        if (!isShiftDown) {
            MutationHandler handler = new MutationHandler(stack);

            AbilityDispatcher dispatcher = new AbilityDispatcher();
            return dispatcher.use(handler, player.getItemInHand(hand), level, player, hand);
        }

        callMenu(serverPlayer, player, stack);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        AbilityDispatcher dispatcher = new AbilityDispatcher();
        MutationHandler handler = new MutationHandler(stack);
        StageHandler stageHandler = new StageHandler(stack);

        dispatcher.releaseUsing(stack, level, entity, timeLeft, handler, stageHandler.getStage(), dispatcher);
        stack.set(DataComponentTypes.CHARGING_SOUND_TYPE, false);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        MutationHandler handler = new MutationHandler(stack);
        boolean hasIrisOut = handler.has(Mutations.IRIS_OUT);

        if (!hasIrisOut) return super.getUseDuration(stack, entity);

        int isActive = handler.getConfigVal(Mutations.Parameters.LIVING_PROJECTILE);

        if (isActive == 0) return super.getUseDuration(stack, entity);

        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        ItemStack stack = player.getMainHandItem();

        if (!canSurvive(stack, level, player)) return false;

        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(entity instanceof ServerPlayer player) || level.isClientSide()) return;

        MawTickData tickData = player.getData(RavenousAttachments.MAW_TICK_DATA);
        tickData.incrementTicks();
        int ticks = tickData.getTickCounter();

        MutationHandler mutationHandler = new MutationHandler(stack);
        StageHandler stageHandler = new StageHandler(stack);

        Stages stage = stageHandler.getStage();

        if (ticks % 10 == 0) {
            mend(stack, mutationHandler);
        }

        if (ticks % DIALOGUE_FREQUENCY == 0 && ticks != 0) {
            talk(stack, player, stage);
        }

        if (ticks >= MAX_TICKS) {
            destabilize(stack, player, stage, mutationHandler);
            tickData.resetTicks();
        }
    }

    private void destabilize(ItemStack stack, Player player, Stages stage, MutationHandler handler) {
        if (stage == Stages.EXCELSIOR) return;

        List<Mutations> muts = new ArrayList<>(handler.matchMutations());

        if (muts.isEmpty()) return;

        Random random = new Random();
        int index = random.nextInt(muts.size());

        muts.get(index).ability().onInstability(handler, stack, player);
    }

    private void talk(ItemStack stack, @NotNull Player player, Stages stage) {
        String dialogue = generateDialogue(stage);
        player.displayClientMessage(Component.literal(dialogue), true);
    }

    private void mend(@NotNull ItemStack stack, MutationHandler handler) {
        if (!stack.isDamaged()) return;

        boolean hasUndyingFlesh = handler.has(Mutations.UNDYING_FLESH);
        if (hasUndyingFlesh) return;

        boolean hasElectricMending = handler.has(Mutations.ELECTRIC_MENDING);
        if (!hasElectricMending) return;

        boolean hasEnoughEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM).getEnergyStored() >= ENERGY_COST_PER_DURABIITY;

        if (!hasEnoughEnergy) return;

        stack.getCapability(Capabilities.EnergyStorage.ITEM).extractEnergy(ENERGY_COST_PER_DURABIITY, false);
        stack.setDamageValue(stack.getDamageValue() - 1);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
        AbilityDispatcher dispatcher = new AbilityDispatcher();
        boolean result = dispatcher.interactLivingEntity(stack, player, entity);

        if (result) {
            // SUCCESS consumes the interaction. PASS would let the pet's own mobInteract run,
            // which sees its new owner clicking and toggles it back to standing.
            return InteractionResult.SUCCESS;
        } else {
            return super.interactLivingEntity(stack, player, entity, hand);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) return true;

        if (oldStack.getItem() != newStack.getItem()) return true;

        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();

        if (!canSurvive(stack, context.getLevel(), context.getPlayer())) return InteractionResult.FAIL;

        if (playerHasShieldUseIntent(context)) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        InteractionResult axeResult = tryAxeActions(context, level, pos, state);
        if (axeResult != InteractionResult.PASS) {
            return axeResult;
        }

        InteractionResult shovelResult = tryShovelActions(context, level, pos, state);
        if (shovelResult != InteractionResult.PASS) {
            return shovelResult;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!(entity instanceof ServerPlayer player)) return;

        int used = stack.getUseDuration(entity) - remainingUseDuration;

        // The component may be missing on a Maw that never went through Iris Out's onCraft (e.g. older saves), so default to "not played".
        boolean alreadyPlayed = stack.getOrDefault(DataComponentTypes.CHARGING_SOUND_TYPE, false);

        if (used >= FULL_CHARGE_TICKS && !alreadyPlayed) {
            stack.set(DataComponentTypes.CHARGING_SOUND_TYPE, true);

            level.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.BEACON_POWER_SELECT,
                SoundSource.PLAYERS,
                0.6F,
                1.2F
            );
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility)
            || ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(itemAbility)
            || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility)
            || ItemAbilities.DEFAULT_SWORD_ACTIONS.contains(itemAbility);
    }

    // todo: add more dialogues in future
    public String generateDialogue(Stages stage) {
        return switch (stage) {
            case LATENT -> Component.translatable("dialogue.ravenousmaw.latent").getString();
            case ADVANCED -> Component.translatable("dialogue.ravenousmaw.advanced").getString();
            case NOBLE -> Component.translatable("dialogue.ravenousmaw.noble").getString();
            case EXCELSIOR -> Component.translatable("dialogue.ravenousmaw.excelsior").getString();
            default -> " ";
        };
    }

    private void callMenu(ServerPlayer serverPlayer, Player player, ItemStack stack) {
        serverPlayer.openMenu(new SimpleMenuProvider(
            (id, inv, entity) -> new ConfigurationMenu(id, player.getInventory(), stack),
            Component.translatable("maw.ravenousmaw.configuration_string")
        ));
    }

    private boolean playerHasShieldUseIntent(UseOnContext context) {
        Player player = context.getPlayer();
        return context.getHand().equals(InteractionHand.MAIN_HAND)
            && player.getOffhandItem().is(Items.SHIELD)
            && !player.isSecondaryUseActive();
    }

    public static List<BlockPos> getBlocksToBeDestroyed(int range, BlockPos initalBlockPos, ServerPlayer player) {
        range = (range - 1) / 2;

        List<BlockPos> positions = new ArrayList<>();

        // Trace as far as the player can actually reach (Tendril Lash extends it), plus a margin so the
        // targeted block is never missed by rounding at the edge of the reach.
        double reach = player.blockInteractionRange() + 1.0;
        BlockHitResult traceResult = player.level().clip(new ClipContext(player.getEyePosition(1f),
            (player.getEyePosition(1f).add(player.getViewVector(1f).scale(reach))),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if(traceResult.getType() == HitResult.Type.MISS) {
            return positions;
        }

        if(traceResult.getDirection() == Direction.DOWN || traceResult.getDirection() == Direction.UP) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY(), initalBlockPos.getZ() + y));
                }
            }
        }

        if(traceResult.getDirection() == Direction.NORTH || traceResult.getDirection() == Direction.SOUTH) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY() + y, initalBlockPos.getZ()));
                }
            }
        }

        if(traceResult.getDirection() == Direction.EAST || traceResult.getDirection() == Direction.WEST) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX(), initalBlockPos.getY() + y, initalBlockPos.getZ() + x));
                }
            }
        }

        return positions;
    }

    public static Set<BlockPos> getBlocksToBeDestroyed(int range, BlockPos initalBlockPos, Player player) {
        range = (range - 1) / 2;

        Set<BlockPos> positions = new HashSet<>();

        // Same reach as the List overload, so the highlight matches what is really broken.
        double reach = player.blockInteractionRange() + 1.0;
        BlockHitResult traceResult = player.level().clip(new ClipContext(player.getEyePosition(1f),
            (player.getEyePosition(1f).add(player.getViewVector(1f).scale(reach))),
            ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if(traceResult.getType() == HitResult.Type.MISS) {
            return positions;
        }

        if(traceResult.getDirection() == Direction.DOWN || traceResult.getDirection() == Direction.UP) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY(), initalBlockPos.getZ() + y));
                }
            }
        }

        if(traceResult.getDirection() == Direction.NORTH || traceResult.getDirection() == Direction.SOUTH) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX() + x, initalBlockPos.getY() + y, initalBlockPos.getZ()));
                }
            }
        }

        if(traceResult.getDirection() == Direction.EAST || traceResult.getDirection() == Direction.WEST) {
            for(int x = -range; x <= range; x++) {
                for(int y = -range; y <= range; y++) {
                    positions.add(new BlockPos(initalBlockPos.getX(), initalBlockPos.getY() + y, initalBlockPos.getZ() + x));
                }
            }
        }

        return positions;
    }

    private InteractionResult tryAxeActions(UseOnContext context, Level level, BlockPos pos, BlockState state) {
        Player player = context.getPlayer();

        BlockState stripped = state.getToolModifiedState(context, ItemAbilities.AXE_STRIP, false);

        if (stripped != null) {
            level.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
            return applyBlockStateChange(context, stripped, pos);
        }

        BlockState scraped = state.getToolModifiedState(context, ItemAbilities.AXE_SCRAPE, false);

        if (scraped != null) {
            level.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3005, pos, 0);
            return applyBlockStateChange(context, scraped, pos);
        }

        BlockState unwaxed = state.getToolModifiedState(context, ItemAbilities.AXE_WAX_OFF, false);

        if (unwaxed != null) {
            level.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.levelEvent(player, 3004, pos, 0);
            return applyBlockStateChange(context, unwaxed, pos);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult tryShovelActions(UseOnContext context, Level level, BlockPos pos, BlockState state) {
        Player player = context.getPlayer();

        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        BlockState flattened = state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);

        if (flattened != null && level.getBlockState(pos.above()).isAir()) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
            return applyBlockStateChange(context, flattened, pos);
        }

        BlockState doused = state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false);

        if (doused != null) {
            if (!level.isClientSide()) {
                level.levelEvent(null, 1009, pos, 0);
            }
            return applyBlockStateChange(context, doused, pos);
        }

        return InteractionResult.PASS;
    }

    private InteractionResult applyBlockStateChange(UseOnContext context, BlockState newState, BlockPos pos) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        }

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            level.setBlock(pos, newState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
        }

        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public static Item.Properties createProperties(Stages stage) {
        return new Item.Properties()
            .durability(stage.getUses())
            .attributes(createAttributes(stage))
            .component(DataComponents.TOOL, createToolComponent(stage))
            .component(DataComponentTypes.MUTATION_COMPONENT_TYPE, MutationComponent.generate());
    }

    private static Tool createToolComponent(Stages stage) {
        return stage.createToolProperties(RavenousBlockTagsProvider.CORRECT_FOR_MAW);
    }

    private static ItemAttributeModifiers createAttributes(Stages stage) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (Constants.MAW_ATTACK_DAMAGE_MODIFIER + stage.getAttackDamageBonus()), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, (Constants.MAW_ATTACK_SPEED_MODIFIER), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .build();
    }

    /**
     * Whether spending {@code cost} durability still leaves the Maw above its protected threshold.
     * Unbreakable Maws (Undying Flesh) never lose durability, so any cost is affordable.
     */
    public static boolean canAfford(ItemStack stack, int cost) {
        if (!stack.isDamageableItem()) return true;

        int remainingDurability = stack.getMaxDamage() - stack.getDamageValue();
        int threshold = (int) (stack.getMaxDamage() * Constants.MAW_DURABILITY_PERCENTAGE_THRESHOLD);

        return remainingDurability - cost > threshold;
    }

    public boolean canSurvive(ItemStack stack, Level level, LivingEntity entity) {
        if (canAfford(stack, 0)) {
            return true;
        }

        if (level.isClientSide && entity instanceof Player player) {
            level.playSound(player, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.5F, 0.5F);
            player.displayClientMessage(DURABILITY_WARNING, true);
        }

        return false;
    }
}
