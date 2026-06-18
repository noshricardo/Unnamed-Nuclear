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
    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final net.neoforged.neoforge.fluids.capability.templates.FluidTank inputTank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(4000);
    private final net.neoforged.neoforge.fluids.capability.templates.FluidTank productTank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(4000);
    private final net.neoforged.neoforge.fluids.capability.templates.FluidTank tailsTank = new net.neoforged.neoforge.fluids.capability.templates.FluidTank(4000);

    private int progress = 0;
    private int maxProgress = 100;

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

    public net.neoforged.neoforge.fluids.capability.templates.FluidTank getInputTank() { return inputTank; }
    public net.neoforged.neoforge.fluids.capability.templates.FluidTank getProductTank() { return productTank; }
    public net.neoforged.neoforge.fluids.capability.templates.FluidTank getTailsTank() { return tailsTank; }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity be) {
        if (level.isClientSide) return;

        if (be.canProcess()) {
            be.progress++;
            if (be.progress >= be.maxProgress) {
                be.process();
                be.progress = 0;
            }
            be.setChanged();
        } else {
            if (be.progress > 0) {
                be.progress = 0;
                be.setChanged();
            }
        }
        
        // Output items to tanks
        be.fillTanksFromInventory();
    }

    private void fillTanksFromInventory() {
        // Product Tank
        ItemStack productStack = inventory.getStackInSlot(1);
        if (!productStack.isEmpty() && productStack.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
            int amount = Math.min(productStack.getCount() * 100, productTank.getCapacity() - productTank.getFluidAmount());
            if (amount >= 100) {
                int toConsume = amount / 100;
                if (productTank.fill(new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), toConsume * 100), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE) > 0) {
                    productStack.shrink(toConsume);
                }
            }
        }
        
        // Tails Tank
        ItemStack tailsStack = inventory.getStackInSlot(2);
        if (!tailsStack.isEmpty() && tailsStack.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
            int amount = Math.min(tailsStack.getCount() * 100, tailsTank.getCapacity() - tailsTank.getFluidAmount());
            if (amount >= 100) {
                int toConsume = amount / 100;
                if (tailsTank.fill(new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), toConsume * 100), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE) > 0) {
                    tailsStack.shrink(toConsume);
                }
            }
        }
    }

    private boolean canProcess() {
        ItemStack input = inventory.getStackInSlot(0);
        
        if (input.isEmpty() || !input.is(Registration.URANIUM_HEXAFLUORIDE.get()) || input.getCount() < 2) {
            if (inputTank.getFluidAmount() < 200 || !inputTank.getFluid().is(Registration.UF6.get())) {
                return false;
            }
        }
        
        return inventory.getStackInSlot(1).getCount() < inventory.getSlotLimit(1) && 
               inventory.getStackInSlot(2).getCount() < inventory.getSlotLimit(2) &&
               productTank.getFluidAmount() <= productTank.getCapacity() - 100 &&
               tailsTank.getFluidAmount() <= tailsTank.getCapacity() - 100;
    }

    private void process() {
        ItemStack inputStack = inventory.getStackInSlot(0);
        com.unnamednuclear.item.NuclearComposition comp;
        
        if (inputStack.getCount() >= 2 && inputStack.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
            comp = inputStack.get(Registration.COMPOSITION.get());
            inputStack.shrink(2);
        } else if (inputTank.getFluidAmount() >= 200 && inputTank.getFluid().is(Registration.UF6.get())) {
            comp = null; 
            inputTank.drain(200, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        } else {
            return;
        }
        
        if (comp == null) comp = new com.unnamednuclear.item.NuclearComposition(0.0071, 0.99285, 0, 0, 0, 0, 0.00005, 0, 0);

        double x_f = comp.u235() / (comp.u235() + comp.u238());
        double alpha = 1.05; 
        double x_p = (alpha * x_f) / (1 + (alpha - 1) * x_f);
        double x_t = x_f / (alpha - (alpha - 1) * x_f); // More accurate tails concentration

        // For a single stage, we assume a symmetric cut (theta = 0.5) for simplicity,
        // or we calculate the cut required for mass balance: theta = (x_f - x_t) / (x_p - x_t)
        double theta = (x_p != x_t) ? (x_f - x_t) / (x_p - x_t) : 0.5;
        theta = Math.max(0.1, Math.min(0.9, theta)); // Keep it reasonable
        
        double uTotal = comp.u235() + comp.u238();
        double othersTotal = comp.getTotal() - uTotal;
        
        // Product Uranium
        double p_uTotal = uTotal * theta;
        double p_u235 = p_uTotal * x_p;
        double p_u238 = p_uTotal * (1.0 - x_p);

        // Tails Uranium
        double t_uTotal = uTotal * (1.0 - theta);
        double t_u235 = t_uTotal * x_t;
        double t_u238 = t_uTotal * (1.0 - x_t);

        // Other isotopes and waste are distributed by mass
        double productScale = theta;
        double tailsScale = 1.0 - theta;

        com.unnamednuclear.item.NuclearComposition productComp = new com.unnamednuclear.item.NuclearComposition(
                p_u235, p_u238, comp.pu239() * productScale, comp.sr90() * productScale, comp.cs137() * productScale, 
                comp.waste() * productScale, comp.u234() * productScale, comp.u236() * productScale, comp.pu240() * productScale);
                
        com.unnamednuclear.item.NuclearComposition tailsComp = new com.unnamednuclear.item.NuclearComposition(
                t_u235, t_u238, comp.pu239() * tailsScale, comp.sr90() * tailsScale, comp.cs137() * tailsScale, 
                comp.waste() * tailsScale, comp.u234() * tailsScale, comp.u236() * tailsScale, comp.pu240() * tailsScale);

        ItemStack productStack = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        productStack.set(Registration.COMPOSITION.get(), productComp.normalize());

        ItemStack tailsStack = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        tailsStack.set(Registration.COMPOSITION.get(), tailsComp.normalize());

        addOrGrow(1, productStack);
        addOrGrow(2, tailsStack);
        
        productTank.fill(new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), 100), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        tailsTank.fill(new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), 100), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
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
        tag.put("InputTank", inputTank.writeToNBT(registries, new CompoundTag()));
        tag.put("ProductTank", productTank.writeToNBT(registries, new CompoundTag()));
        tag.put("TailsTank", tailsTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        if (tag.contains("InputTank")) {
            inputTank.readFromNBT(registries, tag.getCompound("InputTank"));
        }
        if (tag.contains("ProductTank")) {
            productTank.readFromNBT(registries, tag.getCompound("ProductTank"));
        }
        if (tag.contains("TailsTank")) {
            tailsTank.readFromNBT(registries, tag.getCompound("TailsTank"));
        }
        progress = tag.getInt("Progress");
    }
}
