package net.rywir.ravenousmaw.registry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.rywir.ravenousmaw.RavenousMaw;
import net.rywir.ravenousmaw.content.block.MutationMatrixBlock;

import java.util.function.Function;
import java.util.function.Supplier;

public class RavenousBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(RavenousMaw.MOD_ID);

    public static final DeferredBlock<Block> MUTATION_MATRIX = registerBlock("mutation_matrix",
        MutationMatrixBlock::new, () -> BlockBehaviour.Properties.of()
            .strength(4.0F)
            .requiresCorrectToolForDrops()
            .ignitedByLava()
            .sound(SoundType.SLIME_BLOCK)
    );

    // Blocks and block items need an id in their properties, which the registers set for us
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<BlockBehaviour.Properties, T> factory, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> toReturn = BLOCKS.registerBlock(name, factory, properties);
        RavenousItems.ITEMS.registerSimpleBlockItem(name, toReturn);
        return toReturn;
    }

    public static void registerBlocks(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
