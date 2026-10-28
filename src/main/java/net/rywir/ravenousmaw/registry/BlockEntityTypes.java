package net.rywir.ravenousmaw.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.block.entity.MutationMatrixBlockEntity;

import java.util.function.Supplier;

public class BlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, RavenousMaw.MOD_ID);

    public static final Supplier<BlockEntityType<MutationMatrixBlockEntity>> MUTATION_MATRIX_BLOCK_ENTITY_TYPE = BLOCK_ENTITY_TYPES.register("mutation_matrix_block_entity",
        () -> new BlockEntityType<>(MutationMatrixBlockEntity::new, RavenousBlocks.MUTATION_MATRIX.get()));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
