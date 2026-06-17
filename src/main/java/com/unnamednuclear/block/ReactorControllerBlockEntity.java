package com.unnamednuclear.block;

import com.unnamednuclear.client.ClientReactorTracker;
import com.unnamednuclear.registration.Registration;
import com.unnamednuclear.simulation.SimulationNode;
import com.unnamednuclear.simulation.WorldSimulationData;
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
        NOT_ON_SURFACE("not_on_surface"),
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
    }

    private void checkMultiblock() {
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> foundInterior = new ArrayList<>();
        List<BlockPos> foundErrors = new ArrayList<>();
        
        AssemblyResult result = AssemblyResult.SUCCESS;
        int maxSize = 1000;

        // Start from the back of the controller
        Direction facing = getBlockState().getValue(ReactorControllerBlock.FACING);
        BlockPos back = worldPosition.relative(facing.getOpposite());
        
        if (!isCasing(back)) {
            queue.add(back);
            visited.add(back);
        } else {
            result = AssemblyResult.NO_INTERIOR;
        }

        if (result == AssemblyResult.SUCCESS) {
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
        }

        if (result == AssemblyResult.SUCCESS) {
            if (!foundErrors.isEmpty()) {
                result = AssemblyResult.INVALID_BLOCK;
            } else if (foundInterior.isEmpty()) {
                result = AssemblyResult.NO_INTERIOR;
            } else if (foundInterior.contains(worldPosition.relative(facing))) {
                result = AssemblyResult.NOT_ON_SURFACE; // Controller must be on the surface
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
                changed = true;
            }
        } else {
            if (assembled) {
                assembled = false;
                interiorNodes.clear();
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
        return state.isAir() || state.is(Registration.FUEL_CHANNEL.get()) || 
               state.is(Registration.MODERATOR.get()) || state.is(Registration.CONTROL_CHANNEL.get()) ||
               state.is(Registration.COOLANT_CHANNEL.get());
    }


    public boolean isAssembled() {
        return assembled;
    }

    public double getTotalHeat() {
        if (level == null || level.isClientSide) return 0;
        WorldSimulationData data = WorldSimulationData.get((net.minecraft.server.level.ServerLevel) level);
        double totalHeat = 0;
        for (BlockPos pos : interiorNodes) {
            SimulationNode node = data.getNode(pos);
            if (node != null) {
                totalHeat += node.heat;
            }
        }
        return totalHeat;
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
