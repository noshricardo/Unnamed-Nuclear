package com.unnamednuclear.block;

import com.unnamednuclear.client.ClientReactorTracker;
import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ReactorControllerBlockEntity extends BlockEntity implements MenuProvider {
    public enum AssemblyResult {
        SUCCESS("ok"),
        TOO_LARGE("too_large"),
        INVALID_BLOCK("invalid_block"),
        NO_INTERIOR("no_interior"),
        INCOMPLETE("incomplete");

        private final String messageKey;
        AssemblyResult(String messageKey) { this.messageKey = messageKey; }
        public String getMessageKey() { return "tooltip.unnamednuclear.assembly." + messageKey; }
    }

    private boolean assembled = false;
    private boolean active = true;
    private AssemblyResult lastResult = AssemblyResult.NO_INTERIOR;
    private final List<BlockPos> errorPositions = new ArrayList<>();
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
        List<BlockPos> foundErrors = new ArrayList<>();
        
        // Start from neighbors of controller
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = worldPosition.relative(dir);
            if (!isCasing(neighbor)) {
                queue.add(neighbor);
                visited.add(neighbor);
            }
        }

        AssemblyResult result = AssemblyResult.SUCCESS;
        int maxSize = 1000;
        
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            if (isCasing(current)) continue;

            if (current.distManhattan(worldPosition) > 64) {
                result = AssemblyResult.INCOMPLETE;
                foundErrors.add(current);
                break;
            }
            
            if (!isValidInterior(current)) {
                if (foundErrors.size() < 100) {
                    foundErrors.add(current);
                }
            } else {
                foundInterior.add(current);
            }

            if (foundInterior.size() > maxSize) {
                result = AssemblyResult.TOO_LARGE;
                foundErrors.add(current);
                break;
            }
            
            for (Direction dir : Direction.values()) {
                BlockPos next = current.relative(dir);
                if (!visited.contains(next) && !isCasing(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }

        if (result == AssemblyResult.SUCCESS) {
            if (!foundErrors.isEmpty()) {
                result = AssemblyResult.INVALID_BLOCK;
            } else if (foundInterior.isEmpty()) {
                result = AssemblyResult.NO_INTERIOR;
            }
        }

        boolean changed = this.lastResult != result || !this.errorPositions.equals(foundErrors);
        this.lastResult = result;
        this.errorPositions.clear();
        this.errorPositions.addAll(foundErrors);

        if (result == AssemblyResult.SUCCESS) {
            if (!assembled || foundInterior.size() != interiorNodes.size()) {
                assembled = true;
                interiorNodes.clear();
                interiorNodes.addAll(foundInterior);
                simulationData.clear();
                for (BlockPos pos : interiorNodes) {
                    simulationData.put(pos, new BlockData());
                }
                changed = true;
            }
        } else {
            if (assembled) {
                assembled = false;
                interiorNodes.clear();
                simulationData.clear();
                changed = true;
            }
        }

        if (changed && level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            setChanged();
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
                double fission = active ? ((data.thermalNeutrons * 0.1) + 0.01) : 0;
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

    public AssemblyResult getLastResult() {
        return lastResult;
    }

    public BlockPos getErrorPos() {
        return errorPositions.isEmpty() ? BlockPos.ZERO : errorPositions.get(0);
    }

    public List<BlockPos> getErrorPositions() {
        return errorPositions;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.unnamednuclear.reactor_controller");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ReactorMenu(containerId, playerInventory, this);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide) {
            ClientReactorTracker.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide) {
            ClientReactorTracker.remove(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && level.isClientSide) {
            ClientReactorTracker.remove(this);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("assembled", assembled);
        tag.putBoolean("active", active);
        tag.putInt("result", lastResult.ordinal());
        long[] posArray = new long[errorPositions.size()];
        for (int i = 0; i < errorPositions.size(); i++) {
            posArray[i] = errorPositions.get(i).asLong();
        }
        tag.putLongArray("errorPositions", posArray);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        assembled = tag.getBoolean("assembled");
        active = tag.contains("active") ? tag.getBoolean("active") : true;
        if (tag.contains("result")) {
            lastResult = AssemblyResult.values()[tag.getInt("result")];
        }
        errorPositions.clear();
        if (tag.contains("errorPositions")) {
            long[] posArray = tag.getLongArray("errorPositions");
            for (long p : posArray) {
                errorPositions.add(BlockPos.of(p));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
