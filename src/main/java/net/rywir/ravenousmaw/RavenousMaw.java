package net.rywir.ravenousmaw;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.rywir.ravenousmaw.content.decorator.EnergyBarDecorator;
import net.rywir.ravenousmaw.content.entity.model.IrisBulletModel;
import net.rywir.ravenousmaw.content.entity.renderer.IrisBulletRenderer;
import net.rywir.ravenousmaw.content.event.*;
import net.rywir.ravenousmaw.content.gui.screen.ConfigurationScreen;
import net.rywir.ravenousmaw.content.gui.screen.MutationMatrixScreen;
import net.rywir.ravenousmaw.registry.*;
import net.rywir.ravenousmaw.util.Configs;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(RavenousMaw.MOD_ID)
public class RavenousMaw {
    public static final String MOD_ID = "ravenousmaw";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RavenousMaw(@NotNull IEventBus modEventBus, @NotNull ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        // EVENTS
        NeoForge.EVENT_BUS.addListener(LivingDeathEvent.class, RavenousLivingDeathEvent::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.BreakSpeed.class, RavenousBreakSpeedEvent::onBreakSpeedEvent);
        NeoForge.EVENT_BUS.addListener(LivingDropsEvent.class, RavenousLivingDropsEvent::onLivingDropsEvent);
        NeoForge.EVENT_BUS.addListener(BlockDropsEvent.class, RavenousBlockDropsEvent::onBlockDropsEvent);
        NeoForge.EVENT_BUS.addListener(BlockEvent.BreakEvent.class, RavenousBreakEvent::onBreakEvent);
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, RavenousRegisterCommandsEvent::onRegisterCommandsEvent);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.Clone.class, RavenousCloneEvent::onCloneEvent);
        NeoForge.EVENT_BUS.addListener(LivingDamageEvent.Post.class, RavenousLivingDamageEvent::onLivingDamageEvent);
        NeoForge.EVENT_BUS.addListener(AttackEntityEvent.class, RavenousAttackEntityEvent::onAttackEntityEvent);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Pre.class, RavenousPlayerTickEvent::onPlayerTickEvent);

        modEventBus.addListener(RegisterPayloadHandlersEvent.class, RavenousRegisterPayloadHandlersEvent::onRegisterPayloadHandlersEvent);

        // MOD REGISTRATION
        DataComponentTypes.register(modEventBus);

        RavenousItems.registerFeasts(modEventBus);
        RavenousItems.registerItems(modEventBus);
        RavenousItems.registerMaws(modEventBus);

        modEventBus.addListener(RegisterCapabilitiesEvent.class, RavenousRegisterCapabilitiesEvent::onRegisterCapabilitiesEvent);

        MenuTypes.register(modEventBus);

        RavenousBlocks.registerBlocks(modEventBus);
        BlockEntityTypes.register(modEventBus);

        RavenousCreativeModeTabs.register(modEventBus);

        RavenousRecipes.register(modEventBus);

        RavenousMobEffects.register(modEventBus);
        RavenousEntityTypes.register(modEventBus);

        RavenousAttachments.register(modEventBus);

        modEventBus.addListener(this::addCreative);

        // CONFIG REGISTRATION
        modContainer.registerConfig(ModConfig.Type.COMMON, Configs.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {}

    private void addCreative(BuildCreativeModeTabContentsEvent event) {}

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {}

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
			NeoForge.EVENT_BUS.addListener(MovementInputUpdateEvent.class, RavenousMovementInputUpdateEvent::onMovementInputUpdateEvent);
			NeoForge.EVENT_BUS.addListener(RenderHighlightEvent.Block.class, RavenousRenderHighlightEvent::onRenderHighlightEvent);
		}

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(MenuTypes.MUTATION_MATRIX_MENU.get(), MutationMatrixScreen::new);
            event.register(MenuTypes.CONFIGURATION_MENU.get(), ConfigurationScreen::new);
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(IrisBulletModel.LAYER_LOCATION, IrisBulletModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void registerItemDecorators(RegisterItemDecorationsEvent event) {
            event.register(
                RavenousItems.RAVENOUS_MAW_LATENT.get(),
                new EnergyBarDecorator()
            );

            event.register(
                RavenousItems.RAVENOUS_MAW_ADVANCED.get(),
                new EnergyBarDecorator()
            );

            event.register(
                RavenousItems.RAVENOUS_MAW_NOBLE.get(),
                new EnergyBarDecorator()
            );

            event.register(
                RavenousItems.RAVENOUS_MAW_EXCELSIOR.get(),
                new EnergyBarDecorator()
            );
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(
                RavenousEntityTypes.IRIS_BULLET_ENTITY_TYPE.get(),
                IrisBulletRenderer::new
            );
        }
    }
}
