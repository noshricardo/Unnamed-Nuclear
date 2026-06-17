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

    private boolean canProcess(ItemStack input) {
        if (input.isEmpty()) {
            // Electrolysis of HF to F2 (requires no item input, just fluid in tank)
            return fluidTank.getFluid().is(Registration.HF.get()) && fluidTank.getFluidAmount() >= 500;
        }
        
        // Step 0: Fluorite -> HF Acid (assumes water is internal or from atmosphere)
        if (input.is(Registration.FLUORITE.get())) {
            return (fluidTank.isEmpty() || fluidTank.getFluid().is(Registration.HF.get())) && fluidTank.getFluidAmount() <= 3500;
        }

        // Step 0.1: Redstone -> Nitric Acid (simplified)
        if (input.is(net.minecraft.world.item.Items.REDSTONE)) {
            return (fluidTank.isEmpty() || fluidTank.getFluid().is(Registration.HNO3.get())) && fluidTank.getFluidAmount() <= 3500;
        }

        // Step 1: Yellowcake + Nitric Acid -> Uranyl Nitrate
        if (input.is(Registration.YELLOWCAKE.get())) {
             return fluidTank.getFluidAmount() >= 200 && fluidTank.getFluid().is(Registration.HNO3.get()) 
                && inventory.getStackInSlot(1).getCount() < 64;
        }

        // Step 2: Uranyl Nitrate -> Uranium Dioxide (UO2)
        if (input.is(Registration.URANYL_NITRATE.get())) {
            return inventory.getStackInSlot(1).getCount() < 64;
        }
        
        // Step 3: UO2 + 4HF -> UF4
        if (input.is(Registration.URANIUM_DIOXIDE.get())) {
            return fluidTank.getFluidAmount() >= 400 && fluidTank.getFluid().is(Registration.HF.get())
                && inventory.getStackInSlot(1).getCount() < 64;
        }

        // Step 4: UF4 + F2 -> UF6
        if (input.is(Registration.URANIUM_TETRAFLUORIDE.get())) {
            return fluidTank.getFluidAmount() >= 200 && fluidTank.getFluid().is(Registration.F2.get())
                && inventory.getStackInSlot(1).getCount() < 64;
        }

        return false;
    }

    private void process(ItemStack input) {
        if (input.isEmpty()) {
            // HF -> F2
            if (fluidTank.getFluid().is(Registration.HF.get()) && fluidTank.getFluidAmount() >= 500) {
                fluidTank.drain(500, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                fluidTank.fill(new net.neoforged.neoforge.fluids.FluidStack(Registration.F2.get(), 500), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            }
            return;
        }

        ItemStack result = ItemStack.EMPTY;
        int fluidUsed = 0;
        net.neoforged.neoforge.fluids.FluidStack fluidToGenerate = net.neoforged.neoforge.fluids.FluidStack.EMPTY;

        if (input.is(Registration.FLUORITE.get())) {
            fluidToGenerate = new net.neoforged.neoforge.fluids.FluidStack(Registration.HF.get(), 500);
        } else if (input.is(net.minecraft.world.item.Items.REDSTONE)) {
            fluidToGenerate = new net.neoforged.neoforge.fluids.FluidStack(Registration.HNO3.get(), 500);
        } else if (input.is(Registration.YELLOWCAKE.get())) {
            result = new ItemStack(Registration.URANYL_NITRATE.get());
            fluidUsed = 200;
        } else if (input.is(Registration.URANYL_NITRATE.get())) {
            result = new ItemStack(Registration.URANIUM_DIOXIDE.get());
        } else if (input.is(Registration.URANIUM_DIOXIDE.get())) {
            result = new ItemStack(Registration.URANIUM_TETRAFLUORIDE.get());
            fluidUsed = 400;
        } else if (input.is(Registration.URANIUM_TETRAFLUORIDE.get())) {
            result = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
            fluidUsed = 200;
        }

        if (!result.isEmpty() || !fluidToGenerate.isEmpty()) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp == null && !result.isEmpty() && (input.is(Registration.YELLOWCAKE.get()) || input.is(Registration.URANYL_NITRATE.get()) || input.is(Registration.URANIUM_DIOXIDE.get()) || input.is(Registration.URANIUM_TETRAFLUORIDE.get()))) {
                comp = new com.unnamednuclear.item.NuclearComposition(0.007, 0.993, 0, 0, 0, 0, 0, 0, 0);
            }
            
            input.shrink(1);
            if (fluidUsed > 0) {
                fluidTank.drain(fluidUsed, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            }
            if (!result.isEmpty()) {
                if (comp != null) result.set(Registration.COMPOSITION.get(), comp);
                addOrGrow(1, result);
            }
            if (!fluidToGenerate.isEmpty()) {
                fluidTank.fill(fluidToGenerate, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            }
        }
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
