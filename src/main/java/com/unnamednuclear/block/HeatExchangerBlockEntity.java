package com.unnamednuclear.block;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.capabilities.Capabilities;

public class HeatExchangerBlockEntity extends BlockEntity {
    private final FluidTank primaryTank = new FluidTank(4000);
    private final FluidTank waterTank = new FluidTank(4000);
    private final FluidTank steamTank = new FluidTank(4000);
    private double internalHeat = 20;

    public HeatExchangerBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.HEAT_EXCHANGER_BE.get(), pos, state);
    }

    public FluidTank getPrimaryTank() { return primaryTank; }
    public FluidTank getWaterTank() { return waterTank; }
    public FluidTank getSteamTank() { return steamTank; }

    public static void tick(Level level, BlockPos pos, BlockState state, HeatExchangerBlockEntity be) {
        if (level.isClientSide) return;

        // 1. Heat transfer from hot primary coolant
        if (!be.primaryTank.isEmpty() && be.primaryTank.getFluid().is(Registration.HOT_SODIUM.get())) {
            FluidStack hotStack = be.primaryTank.getFluid();
            int amount = Math.min(hotStack.getAmount(), 200);
            
            // Sodium specific heat: ~1.2 J/g*K. 
            // Simplified: 1mB transfers enough energy to heat internal heat.
            double heatEnergy = amount * 5.0;
            
            // Only convert if we have space in the tank for the cooled fluid
            // Since we are draining from the same tank, we need to be careful if it's the same tank.
            // If it's a single tank for both Hot and Cold, convert in place or drain then fill.
            
            int drained = be.primaryTank.drain(amount, IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (drained > 0) {
                be.primaryTank.drain(drained, IFluidHandler.FluidAction.EXECUTE);
                be.primaryTank.fill(new FluidStack(Registration.SODIUM.get(), drained), IFluidHandler.FluidAction.EXECUTE);
                be.internalHeat += (drained * 5.0);
            }
        }

        // 2. Steam generation (Boiling)
        // Water boiling point is 100C (373K)
        if (be.internalHeat > 373 && !be.waterTank.isEmpty()) {
            double deltaT = be.internalHeat - 373;
            int maxBoil = (int) (deltaT * 2); 
            int amountToBoil = Math.min(be.waterTank.getFluidAmount(), Math.min(maxBoil, 500));
            
            if (amountToBoil > 0) {
                int steamAmount = amountToBoil * 20;
                int filled = be.steamTank.fill(new FluidStack(Registration.STEAM.get(), steamAmount), IFluidHandler.FluidAction.SIMULATE);
                if (filled > 0) {
                    int waterToDrain = filled / 20;
                    if (waterToDrain > 0) {
                        be.waterTank.drain(waterToDrain, IFluidHandler.FluidAction.EXECUTE);
                        be.steamTank.fill(new FluidStack(Registration.STEAM.get(), waterToDrain * 20), IFluidHandler.FluidAction.EXECUTE);
                        be.internalHeat -= waterToDrain * 1.5;
                    }
                }
            }
        }

        // 3. Ambient heat loss
        be.internalHeat -= (be.internalHeat - 293) * 0.05;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("primaryTank", primaryTank.writeToNBT(registries, new CompoundTag()));
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("steamTank", steamTank.writeToNBT(registries, new CompoundTag()));
        tag.putDouble("heat", internalHeat);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        primaryTank.readFromNBT(registries, tag.getCompound("primaryTank"));
        waterTank.readFromNBT(registries, tag.getCompound("waterTank"));
        steamTank.readFromNBT(registries, tag.getCompound("steamTank"));
        internalHeat = tag.getDouble("heat");
    }
}
