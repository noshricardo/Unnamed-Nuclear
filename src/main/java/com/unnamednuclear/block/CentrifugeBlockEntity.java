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

import java.util.HashMap;
import java.util.Map;

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
                //if (productTank.fill(new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), toConsume * 100), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE) > 0) {
                    //productStack.shrink(toConsume);
                //}
            }
        }
        
        // Tails Tank
        ItemStack tailsStack = inventory.getStackInSlot(2);
        if (!tailsStack.isEmpty() && tailsStack.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
            int amount = Math.min(tailsStack.getCount() * 100, tailsTank.getCapacity() - tailsTank.getFluidAmount());
            if (amount >= 100) {
                int toConsume = amount / 100;
                //if (tailsTank.fill(new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), toConsume * 100), net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE) > 0) {
                    //tailsStack.shrink(toConsume);
                //}
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
            comp = inputTank.getFluid().get(Registration.COMPOSITION.get()); 
            inputTank.drain(200, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
        } else {
            return;
        }
        
        if (comp == null) comp = new com.unnamednuclear.item.NuclearComposition(Map.of(id("u235"), 0.0071, id("u238"), 0.99285, id("u234"), 0.00005));

        double theta = 0.5; // Symmetric cut
        double alphaBase = 1.1; // Separation factor per mass unit difference
        
        Map<net.minecraft.resources.ResourceLocation, Double> productAmounts = new HashMap<>();
        Map<net.minecraft.resources.ResourceLocation, Double> tailsAmounts = new HashMap<>();

        double productSum = 0;
        double tailsSum = 0;

        for (Map.Entry<net.minecraft.resources.ResourceLocation, Double> entry : comp.amounts().entrySet()) {
            com.unnamednuclear.simulation.Isotope isotope = com.unnamednuclear.simulation.IsotopeRegistry.get(entry.getKey());
            double mass = (isotope != null) ? isotope.atomicMass() : 238.0;
            
            // Separation factor based on mass difference from U-238
            double separation = Math.pow(alphaBase, (238.0 - mass) * 0.1);
            
            double p_val = entry.getValue() * theta * separation;
            double t_val = entry.getValue() * (1.0 - theta) / separation;
            
            productAmounts.put(entry.getKey(), p_val);
            tailsAmounts.put(entry.getKey(), t_val);
            productSum += p_val;
            tailsSum += t_val;
        }

        // Normalize to preserve total mass proportions
        double originalTotal = comp.getTotal();
        for (net.minecraft.resources.ResourceLocation id : productAmounts.keySet()) {
            productAmounts.put(id, (productAmounts.get(id) / productSum) * originalTotal * theta);
        }
        for (net.minecraft.resources.ResourceLocation id : tailsAmounts.keySet()) {
            tailsAmounts.put(id, (tailsAmounts.get(id) / tailsSum) * originalTotal * (1.0 - theta));
        }

        com.unnamednuclear.item.NuclearComposition productComp = new com.unnamednuclear.item.NuclearComposition(productAmounts);
        com.unnamednuclear.item.NuclearComposition tailsComp = new com.unnamednuclear.item.NuclearComposition(tailsAmounts);

        ItemStack productStack = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        productStack.set(Registration.COMPOSITION.get(), productComp);

        ItemStack tailsStack = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        tailsStack.set(Registration.COMPOSITION.get(), tailsComp);

        addOrGrow(1, productStack);
        addOrGrow(2, tailsStack);
        
        net.neoforged.neoforge.fluids.FluidStack productFluid = new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), 100);
        productFluid.set(Registration.COMPOSITION.get(), productComp);
        productTank.fill(productFluid, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);

        net.neoforged.neoforge.fluids.FluidStack tailsFluid = new net.neoforged.neoforge.fluids.FluidStack(Registration.UF6.get(), 100);
        tailsFluid.set(Registration.COMPOSITION.get(), tailsComp);
        tailsTank.fill(tailsFluid, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
    }

    private static net.minecraft.resources.ResourceLocation id(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(com.unnamednuclear.UnnamedNuclear.MODID, path);
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
