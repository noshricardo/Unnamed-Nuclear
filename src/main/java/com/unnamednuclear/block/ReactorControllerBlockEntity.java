package com.unnamednuclear.block;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class ReactorControllerBlockEntity extends BlockEntity {
    private boolean assembled = false;
    private final List<BlockPos> interiorNodes = new ArrayList<>();
    private final Map<BlockPos, BlockData> simulationData = new HashMap<>();
    private int tickCounter = 0;

    public ReactorControllerBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.REACTOR_CONTROLLER_BE.get(), pos, state);
    }

    public void tick() {
        if (level == null || level.isClientSide) return;

        tickCounter++;
        if (tickCounter % 20 == 0) {
            checkMultiblock();
        }

        if (assembled) {
            simulate();
        }
    }

    private void checkMultiblock() {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> foundInterior = new ArrayList<>();
        
        // Start from neighbors of controller
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(dir);
            if (!isCasing(neighbor)) {
                queue.add(neighbor);
                visited.add(neighbor);
            }
        }

        boolean valid = true;
        int maxSize = 1000;
        
        while (!queue.isEmpty()) {
            if (foundInterior.size() > maxSize) {
                valid = false;
                break;
            }
            
            BlockPos current = queue.poll();
            if (isCasing(current)) continue;
            
            if (!isValidInterior(current)) {
                valid = false;
                break;
            }
            
            foundInterior.add(current);
            
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (!visited.contains(next) && !isCasing(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        if (valid && !foundInterior.isEmpty()) {
            if (!assembled || foundInterior.size() != interiorNodes.size()) {
                assembled = true;
                interiorNodes.clear();
                interiorNodes.addAll(foundInterior);
                simulationData.clear();
                for (BlockPos pos : interiorNodes) {
                    simulationData.put(pos, new BlockData());
                }
            }
        } else {
            assembled = false;
            interiorNodes.clear();
            simulationData.clear();
        }
    }

    private boolean isCasing(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Registration.REACTOR_CASING.get()) || state.is(Registration.REACTOR_CONTROLLER.get());
    }

    private boolean isValidInterior(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Registration.FUEL_ROD.get()) || 
               state.is(Registration.MODERATOR.get()) || state.is(Registration.CONTROL_ROD.get());
    }

    private void simulate() {
        // 1. Generation & Transformation
        for (BlockPos pos : interiorNodes) {
            BlockData data = simulationData.get(pos);
            BlockState state = level.getBlockState(pos);

            if (state.is(Registration.FUEL_ROD.get())) {
                double fission = (data.thermalNeutrons * 0.1) + 0.01;
                data.nextFastNeutrons += fission * 2.5;
                data.nextHeat += fission * 20.0;
            } else if (state.is(Registration.MODERATOR.get())) {
                double moderated = data.fastNeutrons * 0.5;
                data.fastNeutrons -= moderated;
                data.thermalNeutrons += moderated;
            } else if (state.is(Registration.CONTROL_ROD.get())) {
                data.fastNeutrons *= 0.1;
                data.thermalNeutrons *= 0.1;
            }
        }

        // 2. Diffusion
        for (BlockPos pos : interiorNodes) {
            BlockData data = simulationData.get(pos);
            
            double diffusedFast = data.fastNeutrons * 0.1;
            double diffusedThermal = data.thermalNeutrons * 0.1;
            double diffusedHeat = data.heat * 0.05;

            data.fastNeutrons -= diffusedFast * 6;
            data.thermalNeutrons -= diffusedThermal * 6;
            data.heat -= diffusedHeat * 6;

            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                BlockData neighborData = simulationData.get(neighborPos);
                if (neighborData != null) {
                    neighborData.nextFastNeutrons += diffusedFast;
                    neighborData.nextThermalNeutrons += diffusedThermal;
                    neighborData.nextHeat += diffusedHeat;
                } else if (isCasing(neighborPos)) {
                    // Heat loss to casing
                    data.heat -= diffusedHeat;
                }
            }
        }

        // 3. Update values
        double totalHeat = 0;
        for (BlockData data : simulationData.values()) {
            data.fastNeutrons = Math.max(0, data.fastNeutrons + data.nextFastNeutrons);
            data.thermalNeutrons = Math.max(0, data.thermalNeutrons + data.nextThermalNeutrons);
            data.heat = Math.max(0, data.heat + data.nextHeat);
            
            data.nextFastNeutrons = 0;
            data.nextThermalNeutrons = 0;
            data.nextHeat = 0;

            // Ambient cooling
            data.heat *= 0.99;
            totalHeat += data.heat;
        }
        
        if (tickCounter % 20 == 0) {
            // UnnamedNuclear.LOGGER.info("Reactor status: Assembled={}, Heat={}, Nodes={}", assembled, totalHeat, interiorNodes.size());
        }
    }

    private static class BlockData {
        double fastNeutrons;
        double thermalNeutrons;
        double heat;

        double nextFastNeutrons;
        double nextThermalNeutrons;
        double nextHeat;
    }

    public boolean isAssembled() {
        return assembled;
    }

    public double getTotalHeat() {
        return simulationData.values().stream().mapToDouble(d -> d.heat).sum();
    }

    public int getInteriorSize() {
        return interiorNodes.size();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("assembled", assembled);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        assembled = tag.getBoolean("assembled");
    }
}
