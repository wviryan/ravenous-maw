package net.rywir.ravenousmaw.registry;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.attachment.AssimilationData;
import net.rywir.ravenousmaw.content.attachment.BossContributors;
import net.rywir.ravenousmaw.content.attachment.MawTickData;

public class RavenousAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, RavenousMaw.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<MawTickData>> MAW_TICK_DATA = ATTACHMENT_TYPES.register("maw_tick_data",
        () -> AttachmentType.builder(() -> new MawTickData())
            .serialize(MawTickData.CODEC)
            .build()
    );

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BossContributors>> BOSS_CONTRIBUTORS = ATTACHMENT_TYPES.register("boss_contributors",
        () -> AttachmentType.builder(() -> new BossContributors())
            .serialize(BossContributors.CODEC)
            .build()
    );

    // copyOnDeath: assimilations are earned deeds, so they must survive respawning
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<AssimilationData>> ASSIMILATIONS = ATTACHMENT_TYPES.register("assimilations",
        () -> AttachmentType.builder(() -> new AssimilationData())
            .serialize(AssimilationData.CODEC)
            .copyOnDeath()
            .build()
    );

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
