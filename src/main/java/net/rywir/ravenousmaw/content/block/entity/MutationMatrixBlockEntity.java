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
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
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

    public final ItemStackHandler itemHandler = new ItemStackHandler(4) {
        // The result slot is output only: crafting fills it through setStackInSlot (which skips
        // this check), and players can only take from it. A foreign item there would block crafting.
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot != RESULT_SLOT;
        }

        @Override
        protected void onContentsChanged(int slot) {
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

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("mutation_matrix.progress", progress);
        tag.putInt("mutation_matrix.max_progress", maxProgress);
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("mutation_matrix.progress");
        maxProgress = tag.getInt("mutation_matrix.max_progress");
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

        if (!itemHandler.getStackInSlot(RESULT_SLOT).isEmpty()) {
            return;
        }

        if (this.cachedRecipe == null) {
            ItemStack maw = itemHandler.getStackInSlot(INPUT_SLOT);
            ItemStack feast = itemHandler.getStackInSlot(FEAST_SLOT);
            ItemStack mutagen = itemHandler.getStackInSlot(MUTAGEN_SLOT);

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
            ItemStack maw = itemHandler.getStackInSlot(INPUT_SLOT);
            ItemStack feast = itemHandler.getStackInSlot(FEAST_SLOT);
            ItemStack mutagen = itemHandler.getStackInSlot(MUTAGEN_SLOT);
            MutationRecipeInput input = new MutationRecipeInput(maw, feast, mutagen);

            craftItem(this.cachedRecipe.value(), input, level.registryAccess());
            resetProgress();

            this.cachedRecipe = null;
        }
    }

    private void craftItem(MutationRecipe recipe, MutationRecipeInput input, HolderLookup.Provider provider) {
        ItemStack finalResult = recipe.assemble(input, provider);

        itemHandler.extractItem(INPUT_SLOT, 1, false);
        itemHandler.extractItem(FEAST_SLOT, 1, false);
        itemHandler.extractItem(MUTAGEN_SLOT, 1, false);
        itemHandler.setStackInSlot(RESULT_SLOT, finalResult);
    }

    private void resetProgress() {
        this.progress = 0;
    }
}
