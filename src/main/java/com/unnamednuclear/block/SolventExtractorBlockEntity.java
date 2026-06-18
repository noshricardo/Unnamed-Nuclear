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
    private final ItemStackHandler inventory = new ItemStackHandler(6) {
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

    public net.neoforged.neoforge.fluids.capability.templates.FluidTank getFluidTank() {
        return fluidTank;
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
        int limit = inventory.getSlotLimit(1);
        
        // Slot 4: Solvent (TBP), Slot 5: Acid (Nitric Acid)
        ItemStack solvent = inventory.getStackInSlot(4);
        ItemStack acid = inventory.getStackInSlot(5);

        // Uranyl/Plutonium Nitrate + TBP -> Separated Nitrates
        if (input.is(Registration.URANYL_NITRATE.get()) || input.is(Registration.PLUTONIUM_NITRATE.get())) {
            return !solvent.isEmpty() && solvent.is(Registration.TBP_KEROSENE.get())
                && inventory.getStackInSlot(1).getCount() < limit;
        }

        // Nitric Acid Dissolution: Spent Fuel -> Nitrates
        if (input.is(Registration.NUCLEAR_FUEL.get())) {
            com.unnamednuclear.item.NuclearComposition comp = input.get(Registration.COMPOSITION.get());
            if (comp == null) return false;
            return !acid.isEmpty() && acid.is(Registration.NITRIC_ACID.get())
                && inventory.getStackInSlot(1).getCount() < limit && inventory.getStackInSlot(2).getCount() < limit
                && inventory.getStackInSlot(3).getCount() < limit;
        }

        return false;
    }

    private void process(ItemStack input) {
        ItemStack solvent = inventory.getStackInSlot(4);
        ItemStack acid = inventory.getStackInSlot(5);

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
            acid.shrink(1);
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
            solvent.shrink(1);
            addOrGrow(1, uo2);
        } else if (input.is(Registration.PLUTONIUM_NITRATE.get())) {
             // Plutonium Nitrate + TBP -> Pure Plutonium (simplified)
            ItemStack pu = new ItemStack(Registration.PLUTONIUM.get());
            pu.set(Registration.COMPOSITION.get(), input.get(Registration.COMPOSITION.get()));
            input.shrink(1);
            solvent.shrink(1);
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
