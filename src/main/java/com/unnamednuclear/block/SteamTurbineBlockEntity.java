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
    private final EnergyStorage energyStorage = new EnergyStorage(100000, 1000, 1000);

    public SteamTurbineBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.STEAM_TURBINE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamTurbineBlockEntity be) {
        if (level.isClientSide) return;

        if (!be.steamTank.isEmpty()) {
            int steamConsumed = Math.min(be.steamTank.getFluidAmount(), 100);
            be.steamTank.drain(steamConsumed, IFluidHandler.FluidAction.EXECUTE);
            be.energyStorage.receiveEnergy(steamConsumed * 10, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("steamTank", steamTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("energy", energyStorage.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        steamTank.readFromNBT(registries, tag.getCompound("steamTank"));
        // EnergyStorage.receiveEnergy is the way to set it usually if not custom, 
        // but for loading we might need a custom EnergyStorage implementation to set it directly.
        // Simplified for this PoC.
    }
}
