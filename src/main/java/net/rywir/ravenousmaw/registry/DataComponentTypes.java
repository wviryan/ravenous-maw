package net.rywir.ravenousmaw.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.component.MutationComponent;

public class DataComponentTypes {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, RavenousMaw.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MutationComponent>> MUTATION_COMPONENT_TYPE = DATA_COMPONENT_TYPES.register("mutation_component",
        () -> DataComponentType.<MutationComponent>builder()
            .persistent(MutationComponent.CODEC)
            .networkSynchronized(MutationComponent.STREAM_CODEC)
            .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> VORACITY_COMPONENT_TYPE = DATA_COMPONENT_TYPES.register("voracity_component",
        () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> CHARGING_SOUND_TYPE = DATA_COMPONENT_TYPES.register("charging_sound",
        () -> DataComponentType.<Boolean>builder()
            .persistent(Codec.BOOL)
            .networkSynchronized(ByteBufCodecs.BOOL)
            .build()
    );

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_ENERGY = DATA_COMPONENT_TYPES.register("stored_energy",
        () -> DataComponentType.<Integer>builder()
            .persistent(Codec.INT)
            .networkSynchronized(ByteBufCodecs.VAR_INT)
            .build()
    );

    public static void register(IEventBus eventBus) {
        DATA_COMPONENT_TYPES.register(eventBus);
    }
}
