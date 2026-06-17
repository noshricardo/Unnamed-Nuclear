package com.unnamednuclear.block;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class CentrifugeBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final net.neoforged.neoforge.fluids.capability.templates.FluidTank fluidTank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private int progress = 0;
    private int maxProgress = 200; // 10 seconds at 20 tps

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CentrifugeBlockEntity.this.progress;
                case 1 -> CentrifugeBlockEntity.this.maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CentrifugeBlockEntity.this.progress = value;
                case 1 -> CentrifugeBlockEntity.this.maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.CENTRIFUGE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity be) {
        if (level.isClientSide) return;

        ItemStack input = be.inventory.getStackInSlot(0);
        if (be.canProcess(input)) {
            be.progress++;
            if (be.progress >= be.maxProgress) {
                be.process(input);
                be.progress = 0;
            }
            be.setChanged();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.setChanged();
            }
        }
    }

    private boolean canProcess(ItemStack input) {
        if (input.isEmpty()) return false;
        
        // Enrichment: UF6 gas enrichment
        if (input.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
            if (input.getCount() < 2) return false;
            
            ItemStack enrichedSlot = inventory.getStackInSlot(1);
            ItemStack depletedSlot = inventory.getStackInSlot(2);
            
            boolean canFitEnriched = enrichedSlot.isEmpty() || (enrichedSlot.is(Registration.URANIUM_HEXAFLUORIDE.get()) && enrichedSlot.getCount() < 64);
            boolean canFitDepleted = depletedSlot.isEmpty() || (depletedSlot.is(Registration.URANIUM_HEXAFLUORIDE.get()) && depletedSlot.getCount() < 64);
            
            return canFitEnriched && canFitDepleted;
        }

        return false;
    }

    private void process(ItemStack input) {
        if (input.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp == null) comp = new com.unnamednuclear.item.NuclearComposition(0.0071, 0.99285, 0, 0, 0, 0, 0.00005, 0, 0);

            input.shrink(2);
            
            // Separation logic (cascade stage)
            double x_f = comp.u235() / (comp.u235() + comp.u238());
            double alpha = 1.1; // Realistic stage separation factor is actually smaller (~1.004), but for gameplay 1.1 is better
            double x_p = (alpha * x_f) / (1 + (alpha - 1) * x_f);
            
            double totalU235 = comp.u235() * 2;
            double enrichedU235 = x_p; 
            double depletedU235 = Math.max(0.001, totalU235 - enrichedU235);
            
            double enrichedU238 = 1.0 - enrichedU235;
            double depletedU238 = 1.0 - depletedU235;
            
            // Normalize
            double sumE = enrichedU235 + enrichedU238;
            enrichedU235 /= sumE;
            enrichedU238 /= sumE;
            
            double sumD = depletedU235 + depletedU238;
            depletedU235 /= sumD;
            depletedU238 /= sumD;
            
            ItemStack enrichedResult = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
            enrichedResult.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(enrichedU235, enrichedU238, 0, 0, 0, 0, comp.u234(), comp.u236(), 0));
            
            ItemStack depletedResult = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
            depletedResult.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(depletedU235, depletedU238, 0, 0, 0, 0, comp.u234(), comp.u236(), 0));
            
            addOrGrow(1, enrichedResult);
            addOrGrow(2, depletedResult);
        }
    }

    private void addOrGrow(int slot, ItemStack stack) {
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) {
            inventory.setStackInSlot(slot, stack);
        } else {
            existing.grow(stack.getCount());
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.unnamednuclear.centrifuge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CentrifugeMenu(containerId, playerInventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.put("FluidTank", fluidTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        if (tag.contains("FluidTank")) {
            fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        }
        progress = tag.getInt("Progress");
    }
}
