package net.rywir.ravenousmaw.content.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.rywir.ravenousmaw.content.gui.menu.MutationMatrixMenu;
import net.rywir.ravenousmaw.content.recipe.matrix.MutationRecipe;
import net.rywir.ravenousmaw.content.recipe.matrix.MutationRecipeInput;
import net.rywir.ravenousmaw.registry.BlockEntityTypes;
import net.rywir.ravenousmaw.registry.RavenousRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class MutationMatrixBlockEntity extends BlockEntity implements MenuProvider {
    private @Nullable RecipeHolder<MutationRecipe> cachedRecipe = null;

    private static final int INPUT_SLOT = 0;
    private static final int FEAST_SLOT = 1;
    private static final int MUTAGEN_SLOT = 2;
    private static final int RESULT_SLOT = 3;

    private int progress = 0;
    private int maxProgress = 72;

    protected final ContainerData data;

    public final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(4) {
        // The result slot is output only: crafting fills it through set (which skips this
        // check), and players can only take from it. A foreign item there would block crafting.
        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return slot != RESULT_SLOT;
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            setChanged();
            resetProgress();

            cachedRecipe = null;

            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 4);
            }
        }
    };

    public MutationMatrixBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityTypes.MUTATION_MATRIX_BLOCK_ENTITY_TYPE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> MutationMatrixBlockEntity.this.progress;
                    case 1 -> MutationMatrixBlockEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0:
                        MutationMatrixBlockEntity.this.progress = value;
                        break;
                    case 1:
                        MutationMatrixBlockEntity.this.maxProgress = value;
                        break;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.ravenousmaw.mutation_matrix");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new MutationMatrixMenu(id, inv, this, this.data);
    }

    // Called by the chunk (server side only) when the block is replaced, so the contents drop.
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        Containers.dropContents(this.level, pos, itemHandler.copyToList());
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.serialize(output.child("inventory"));
        output.putInt("mutation_matrix.progress", progress);
        output.putInt("mutation_matrix.max_progress", maxProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("inventory").ifPresent(itemHandler::deserialize);
        progress = input.getIntOr("mutation_matrix.progress", 0);
        maxProgress = input.getIntOr("mutation_matrix.max_progress", 72);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if (level.isClientSide()) {
            return;
        }

        if (!getStack(RESULT_SLOT).isEmpty()) {
            return;
        }

        if (this.cachedRecipe == null) {
            ItemStack maw = getStack(INPUT_SLOT);
            ItemStack feast = getStack(FEAST_SLOT);
            ItemStack mutagen = getStack(MUTAGEN_SLOT);

            if (maw.isEmpty() || feast.isEmpty() || mutagen.isEmpty()) {
                resetProgress();
                return;
            }

            MutationRecipeInput input = new MutationRecipeInput(maw, feast, mutagen);

            Optional<RecipeHolder<MutationRecipe>> recipeOptional =
                level.getServer().getRecipeManager().getRecipeFor(RavenousRecipes.MUTATION_RECIPE_TYPE.get(), input, level);

            this.cachedRecipe = recipeOptional.orElse(null);
        }

        if (this.cachedRecipe == null) {
            resetProgress();
            return;
        }

        this.progress++;
        setChanged(level, blockPos, blockState);

        if (this.progress >= this.maxProgress) {
            ItemStack maw = getStack(INPUT_SLOT);
            ItemStack feast = getStack(FEAST_SLOT);
            ItemStack mutagen = getStack(MUTAGEN_SLOT);
            MutationRecipeInput input = new MutationRecipeInput(maw, feast, mutagen);

            craftItem(this.cachedRecipe.value(), input, level.registryAccess());
            resetProgress();

            this.cachedRecipe = null;
        }
    }

    private ItemStack getStack(int slot) {
        return itemHandler.getResource(slot).toStack(itemHandler.getAmountAsInt(slot));
    }

    private void craftItem(MutationRecipe recipe, MutationRecipeInput input, HolderLookup.Provider provider) {
        ItemStack finalResult = recipe.assemble(input, provider);

        shrink(INPUT_SLOT);
        shrink(FEAST_SLOT);
        shrink(MUTAGEN_SLOT);
        itemHandler.set(RESULT_SLOT, ItemResource.of(finalResult), finalResult.getCount());
    }

    private void shrink(int slot) {
        try (Transaction transaction = Transaction.openRoot()) {
            itemHandler.extract(slot, itemHandler.getResource(slot), 1, transaction);
            transaction.commit();
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }
}
