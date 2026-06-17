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

public class SolventExtractorBlockEntity extends BlockEntity implements MenuProvider {
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
    private int maxProgress = 150;

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

    public SolventExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.SOLVENT_EXTRACTOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolventExtractorBlockEntity be) {
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
        
        // Uranyl/Plutonium Nitrate + TBP -> Separated Nitrates
        if (input.is(Registration.URANYL_NITRATE.get()) || input.is(Registration.PLUTONIUM_NITRATE.get())) {
            return fluidTank.getFluidAmount() >= 500 && fluidTank.getFluid().is(Registration.TBP.get())
                && inventory.getStackInSlot(1).getCount() < 64;
        }

        // Nitric Acid Dissolution: Spent Fuel -> Nitrates
        if (input.is(Registration.NUCLEAR_FUEL.get())) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp == null) return false;
            return fluidTank.getFluidAmount() >= 1000 && fluidTank.getFluid().is(Registration.HNO3.get())
                && inventory.getStackInSlot(1).getCount() < 64 && inventory.getStackInSlot(2).getCount() < 64
                && inventory.getStackInSlot(3).getCount() < 64;
        }

        // Uranyl/Plutonium Nitrate + Solidification -> Items
        if (input.is(Registration.URANYL_NITRATE.get()) || input.is(Registration.PLUTONIUM_NITRATE.get())) {
             // If player just wants to solidify without TBP (not PUREX but needed for cycle)
             // We'll prioritize TBP if present in tank, otherwise maybe a different machine?
             // Let's stick to PUREX here.
        }

        return false;
    }

    private void process(ItemStack input) {
        if (input.is(Registration.NUCLEAR_FUEL.get())) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp == null) return;
            double total = comp.getTotal();
            
            ItemStack uNitrate = new ItemStack(Registration.URANYL_NITRATE.get());
            uNitrate.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(
                comp.u235() / total, comp.u238() / total, 0, 0, 0, 0, 
                comp.u234() / total, comp.u236() / total, 0
            ));

            ItemStack puNitrate = new ItemStack(Registration.PLUTONIUM_NITRATE.get());
            puNitrate.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(
                0, 0, comp.pu239() / total, 0, 0, 0, 0, 0, comp.pu240() / total
            ));

            input.shrink(1);
            fluidTank.drain(1000, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            addOrGrow(1, uNitrate);
            addOrGrow(2, puNitrate);
            
            // Generate some liquid waste (simplified as fission products in output 3)
            if (comp.waste() > 0.01) {
                addOrGrow(3, new ItemStack(Registration.FISSION_PRODUCTS.get()));
            }
        } else if (input.is(Registration.URANYL_NITRATE.get())) {
            // Uranyl Nitrate + TBP -> Pure Uranium Dioxide (simplified intermediate)
            ItemStack uo2 = new ItemStack(Registration.URANIUM_DIOXIDE.get());
            uo2.set(Registration.COMPOSITION.get(), input.get(Registration.COMPOSITION.get()));
            input.shrink(1);
            fluidTank.drain(500, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            addOrGrow(1, uo2);
        } else if (input.is(Registration.PLUTONIUM_NITRATE.get())) {
             // Plutonium Nitrate + TBP -> Pure Plutonium (simplified)
            ItemStack pu = new ItemStack(Registration.PLUTONIUM.get());
            pu.set(Registration.COMPOSITION.get(), input.get(Registration.COMPOSITION.get()));
            input.shrink(1);
            fluidTank.drain(500, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            addOrGrow(1, pu);
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
        return Component.translatable("container.unnamednuclear.solvent_extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SolventExtractorMenu(containerId, playerInventory, this, data);
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
