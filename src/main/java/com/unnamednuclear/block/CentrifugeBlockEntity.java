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
        if (input.is(Registration.URANIUM_HEXAFLUORIDE.get()) || input.is(Registration.ENRICHED_URANIUM.get())) {
            ItemStack outputSlot = inventory.getStackInSlot(1);
            return outputSlot.isEmpty() || (outputSlot.is(Registration.ENRICHED_URANIUM.get()) && outputSlot.getCount() < outputSlot.getMaxStackSize());
        }
        if (input.is(Registration.NUCLEAR_FUEL.get())) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp != null && comp.waste() > 0.1) {
                return inventory.getStackInSlot(1).getCount() < 64 && inventory.getStackInSlot(2).getCount() < 64 && inventory.getStackInSlot(3).getCount() < 64;
            }
        }
        return false;
    }

    private void process(ItemStack input) {
        if (input.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
            input.shrink(1);
            ItemStack result = new ItemStack(Registration.ENRICHED_URANIUM.get());
            result.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(0.05, 0.95, 0, 0));
            addOrGrow(1, result);
        } else if (input.is(Registration.ENRICHED_URANIUM.get())) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp != null) {
                input.shrink(1);
                double newU235 = Math.min(0.95, comp.u235() + 0.05);
                double newU238 = Math.max(0, comp.u238() - 0.05);
                ItemStack result = new ItemStack(Registration.ENRICHED_URANIUM.get());
                result.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(newU235, newU238, 0, 0));
                addOrGrow(1, result);
            }
        } else if (input.is(Registration.NUCLEAR_FUEL.get())) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp != null && comp.waste() > 0.1) {
                input.shrink(1);
                if (comp.u235() + comp.u238() > 0.1) {
                    ItemStack u = new ItemStack(Registration.ENRICHED_URANIUM.get());
                    u.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(comp.u235(), comp.u238(), 0, 0));
                    addOrGrow(1, u);
                }
                if (comp.pu239() > 0.01) {
                    addOrGrow(2, new ItemStack(Registration.PLUTONIUM.get()));
                }
                if (comp.waste() > 0.01) {
                    addOrGrow(3, new ItemStack(Registration.FISSION_PRODUCTS.get()));
                }
            }
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
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        progress = tag.getInt("Progress");
    }
}
