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

    public static void tick(Level level, BlockPos pos, BlockState state, HeatExchangerBlockEntity be) {
        if (level.isClientSide) return;

        // 1. Heat transfer from primary coolant
        if (!be.primaryTank.isEmpty()) {
            FluidStack stack = be.primaryTank.getFluid();
            // Simple model: 1mB of primary coolant transfers some heat
            double transfer = stack.getAmount() * 0.01 * (stack.getFluid().getFluidType().getTemperature() - be.internalHeat);
            be.internalHeat += transfer;
        }

        // 2. Steam generation
        if (be.internalHeat > 373 && !be.waterTank.isEmpty()) { // 100C
            int amountToBoil = Math.min(be.waterTank.getFluidAmount(), 100);
            double heatRequired = amountToBoil * 2.0; // Latent heat etc simplified
            if (be.internalHeat > heatRequired) {
                be.waterTank.drain(amountToBoil, IFluidHandler.FluidAction.EXECUTE);
                be.steamTank.fill(new FluidStack(Registration.STEAM.get(), amountToBoil * 10), IFluidHandler.FluidAction.EXECUTE);
                be.internalHeat -= heatRequired;
            }
        }

        // 3. Ambient loss
        be.internalHeat -= (be.internalHeat - 293) * 0.01;
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
