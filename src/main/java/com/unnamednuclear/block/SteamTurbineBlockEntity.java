package com.unnamednuclear.block;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SteamTurbineBlockEntity extends BlockEntity {
    private final FluidTank steamTank = new FluidTank(8000);
    private final FluidTank waterTank = new FluidTank(8000);
    private final EnergyStorage energyStorage = new EnergyStorage(1000000, 10000, 10000);
    private double rotationSpeed = 0;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.STEAM_TURBINE_BE.get(), pos, state);
    }

    public FluidTank getSteamTank() { return steamTank; }
    public FluidTank getWaterTank() { return waterTank; }
    public EnergyStorage getEnergyStorage() { return energyStorage; }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity be) {
        if (level.isClientSide) return;

        if (!be.steamTank.isEmpty() && be.steamTank.getFluid().is(Registration.STEAM.get())) {
            int maxConsume = 200;
            int steamConsumed = Math.min(be.steamTank.getFluidAmount(), maxConsume);
            
            // Work done based on volumetric flow (simplified)
            double work = steamConsumed * 5.0; 
            be.rotationSpeed = Math.min(100, be.rotationSpeed + work * 0.01);
            
            be.steamTank.drain(steamConsumed, IFluidHandler.FluidAction.EXECUTE);
            
            // Condensation: Steam -> Water (10:1 ratio for simplicity, ensure we have space)
            int waterProduced = steamConsumed / 10;
            if (waterProduced > 0) {
                int filled = be.waterTank.fill(new net.neoforged.neoforge.fluids.FluidStack(net.minecraft.world.level.material.Fluids.WATER, waterProduced), IFluidHandler.FluidAction.EXECUTE);
                // If we couldn't fit all condensed water, too bad, it vents or something (simplified)
            }
        } else {
            be.rotationSpeed *= 0.95; // Friction
        }

        if (be.rotationSpeed > 1) {
            int energyGen = (int) (be.rotationSpeed * 50);
            be.energyStorage.receiveEnergy(energyGen, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("steamTank", steamTank.writeToNBT(registries, new CompoundTag()));
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
        tag.putDouble("rotation", rotationSpeed);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("steamTank")) steamTank.readFromNBT(registries, tag.getCompound("steamTank"));
        if (tag.contains("waterTank")) waterTank.readFromNBT(registries, tag.getCompound("waterTank"));
        // Energy handled via custom storage or just receive for simplicity in PoC
        rotationSpeed = tag.getDouble("rotation");
    }
}
