package com.unnamednuclear.block;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
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

public class ChemicalConverterBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(3) {
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
    private int maxProgress = 100;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? progress : maxProgress;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) progress = value;
            else maxProgress = value;
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public ChemicalConverterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.CHEMICAL_CONVERTER_BE.get(), pos, state);
    }

    public net.neoforged.neoforge.fluids.capability.templates.FluidTank getFluidTank() {
        return fluidTank;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalConverterBlockEntity be) {
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

    private void process(ItemStack input) {
        ItemStack reagent = inventory.getStackInSlot(2);
        ItemStack result = ItemStack.EMPTY;

        if (input.is(Registration.FLUORITE.get())) {
            result = new ItemStack(Registration.HYDROFLUORIC_ACID.get());
        } else if (input.is(net.minecraft.world.item.Items.REDSTONE)) {
            result = new ItemStack(Registration.NITRIC_ACID.get());
        } else if (input.is(Registration.YELLOWCAKE.get())) {
            result = new ItemStack(Registration.URANYL_NITRATE.get());
            reagent.shrink(1);
        } else if (input.is(Registration.URANYL_NITRATE.get())) {
            result = new ItemStack(Registration.URANIUM_DIOXIDE.get());
        } else if (input.is(Registration.URANIUM_DIOXIDE.get())) {
            result = new ItemStack(Registration.URANIUM_TETRAFLUORIDE.get());
            reagent.shrink(1);
        } else if (input.is(Registration.URANIUM_TETRAFLUORIDE.get())) {
            result = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
            reagent.shrink(1);
        } else if (input.is(Registration.HYDROFLUORIC_ACID.get())) {
            result = new ItemStack(Registration.FLUORINE_GAS.get());
        }

        if (!result.isEmpty()) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp == null && (input.is(Registration.YELLOWCAKE.get()) || input.is(Registration.URANYL_NITRATE.get()) || input.is(Registration.URANIUM_DIOXIDE.get()) || input.is(Registration.URANIUM_TETRAFLUORIDE.get()))) {
                comp = new com.unnamednuclear.item.NuclearComposition(java.util.Map.of(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unnamednuclear", "u235"), 0.007,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unnamednuclear", "u238"), 0.993
                ));
            }
            
            input.shrink(1);
            if (comp != null) result.set(Registration.COMPOSITION.get(), comp);
            addOrGrow(1, result);
        }
    }

    private boolean canProcess(ItemStack input) {
        if (input.isEmpty()) return false;
        int limit = inventory.getSlotLimit(1);
        
        // Step 0: Fluorite -> HF Acid
        if (input.is(Registration.FLUORITE.get())) {
            return inventory.getStackInSlot(1).getCount() < limit;
        }

        // Step 0.1: Redstone -> Nitric Acid
        if (input.is(net.minecraft.world.item.Items.REDSTONE)) {
            return inventory.getStackInSlot(1).getCount() < limit;
        }

        // Slot 2: Chemical Reagent (HF, F2, Nitric Acid)
        ItemStack reagent = inventory.getStackInSlot(2);

        // Step 1: Yellowcake + Nitric Acid -> Uranyl Nitrate
        if (input.is(Registration.YELLOWCAKE.get())) {
             return !reagent.isEmpty() && reagent.is(Registration.NITRIC_ACID.get()) 
                && inventory.getStackInSlot(1).getCount() < limit;
        }

        // Step 2: Uranyl Nitrate -> Uranium Dioxide (UO2)
        if (input.is(Registration.URANYL_NITRATE.get())) {
            return inventory.getStackInSlot(1).getCount() < limit;
        }
        
        // Step 3: UO2 + HF -> UF4
        if (input.is(Registration.URANIUM_DIOXIDE.get())) {
            return !reagent.isEmpty() && reagent.is(Registration.HYDROFLUORIC_ACID.get())
                && inventory.getStackInSlot(1).getCount() < limit;
        }

        // Step 4: UF4 + F2 -> UF6
        if (input.is(Registration.URANIUM_TETRAFLUORIDE.get())) {
            return !reagent.isEmpty() && reagent.is(Registration.FLUORINE_GAS.get())
                && inventory.getStackInSlot(1).getCount() < limit;
        }
        
        // Step 5: Electrolysis of HF -> F2
        if (input.is(Registration.HYDROFLUORIC_ACID.get())) {
            return inventory.getStackInSlot(1).getCount() < limit;
        }

        return false;
    }

    private void addOrGrow(int slot, ItemStack stack) {
        ItemStack existing = inventory.getStackInSlot(slot);
        if (existing.isEmpty()) {
            inventory.setStackInSlot(slot, stack.copy());
        } else {
            existing.grow(stack.getCount());
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.unnamednuclear.chemical_converter");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChemicalConverterMenu(containerId, playerInventory, this, data);
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
