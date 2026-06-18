package com.unnamednuclear.block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.List;
import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.item.ItemStack;

public class ReactorMenu extends AbstractContainerMenu {
    private final ReactorControllerBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    private int syncedAssembled;
    private int syncedHeat;
    private int syncedSize;
    private int syncedLastResult;
    private int syncedActive;
    private int syncedErrorX, syncedErrorY, syncedErrorZ, syncedErrorCount;
    private int syncedFlux;
    private int syncedXenon;

    public ReactorMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (ReactorControllerBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public ReactorMenu(int containerId, Inventory playerInventory, ReactorControllerBlockEntity blockEntity) {
        super(Registration.REACTOR_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        
        layoutPlayerInventorySlots(playerInventory, 8, 84);
        
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.isAssembled() ? 1 : 0; }
            @Override public void set(int value) { syncedAssembled = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return (int) (blockEntity.getTotalHeat() * 100); }
            @Override public void set(int value) { syncedHeat = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getInteriorSize(); }
            @Override public void set(int value) { syncedSize = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getLastResult().ordinal(); }
            @Override public void set(int value) { syncedLastResult = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.isActive() ? 1 : 0; }
            @Override public void set(int value) { syncedActive = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getErrorPos().getX(); }
            @Override public void set(int value) { syncedErrorX = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getErrorPos().getY(); }
            @Override public void set(int value) { syncedErrorY = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getErrorPos().getZ(); }
            @Override public void set(int value) { syncedErrorZ = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return blockEntity.getErrorPositions().size(); }
            @Override public void set(int value) { syncedErrorCount = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return (int) (blockEntity.getNetFlux() * 100); }
            @Override public void set(int value) { syncedFlux = value; }
        });
        addDataSlot(new DataSlot() {
            @Override public int get() { return (int) (blockEntity.getAverageXenon() * 1000000); }
            @Override public void set(int value) { syncedXenon = value; }
        });
    }

    public boolean isAssembled() { return (blockEntity != null && !blockEntity.getLevel().isClientSide) ? blockEntity.isAssembled() : syncedAssembled != 0; }
    public double getHeat() { return (blockEntity != null && !blockEntity.getLevel().isClientSide) ? blockEntity.getTotalHeat() : syncedHeat / 100.0; }
    public double getFlux() { return (blockEntity != null && !blockEntity.getLevel().isClientSide) ? blockEntity.getNetFlux() : syncedFlux / 100.0; }
    public double getXenon() { return (blockEntity != null && !blockEntity.getLevel().isClientSide) ? blockEntity.getAverageXenon() : syncedXenon / 1000000.0; }
    public int getInteriorSize() { return (blockEntity != null && !blockEntity.getLevel().isClientSide) ? blockEntity.getInteriorSize() : syncedSize; }
    public boolean isActive() { return (blockEntity != null && !blockEntity.getLevel().isClientSide) ? blockEntity.isActive() : syncedActive != 0; }
    
    public BlockPos getErrorPos() {
        if (blockEntity != null && !blockEntity.getLevel().isClientSide) return blockEntity.getErrorPos();
        return new BlockPos(syncedErrorX, syncedErrorY, syncedErrorZ);
    }

    public int getErrorCount() {
        if (blockEntity != null && !blockEntity.getLevel().isClientSide) return blockEntity.getErrorPositions().size();
        return syncedErrorCount;
    }
    
    public ReactorControllerBlockEntity.AssemblyResult getLastResult() { 
        if (blockEntity != null && !blockEntity.getLevel().isClientSide) return blockEntity.getLastResult();
        return ReactorControllerBlockEntity.AssemblyResult.values()[syncedLastResult];
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0 && blockEntity != null) {
            blockEntity.setActive(!blockEntity.isActive());
            return true;
        }
        if (id >= 100 && blockEntity != null) {
            // Channel interaction
            // Format: 100 + (action * 10000) + (index)
            // Action 0: Extract fuel
            // Action 1: Insert fuel from hand
            // Action 2: Adjust insertion
            int action = (id - 100) / 10000;
            int index = (id - 100) % 10000;
            
            List<BlockPos> interior = blockEntity.getInteriorNodes();
            if (index >= 0 && index < interior.size()) {
                BlockPos pos = interior.get(index);
                if (action == 0) {
                    if (blockEntity.getLevel().getBlockEntity(pos) instanceof ReactorChannelBlockEntity channel) {
                        ItemStack stack = channel.getItem();
                        if (!stack.isEmpty()) {
                            if (player.getInventory().add(stack)) {
                                channel.setItem(ItemStack.EMPTY);
                            }
                        }
                    }
                } else if (action == 1) {
                    if (blockEntity.getLevel().getBlockEntity(pos) instanceof ReactorChannelBlockEntity channel) {
                        ItemStack stack = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
                        if (!stack.isEmpty() && channel.getItem().isEmpty()) {
                            ItemStack toInsert = stack.copy();
                            toInsert.setCount(1);
                            channel.setItem(toInsert);
                            stack.shrink(1);
                        }
                    }
                } else if (action == 2) {
                    BlockState state = blockEntity.getLevel().getBlockState(pos);
                    if (state.hasProperty(ReactorChannelBlock.INSERTION)) {
                        int current = state.getValue(ReactorChannelBlock.INSERTION);
                        int next = (current + 1) % 11;
                        blockEntity.getLevel().setBlock(pos, state.setValue(ReactorChannelBlock.INSERTION, next), 3);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public List<BlockPos> getInteriorNodes() {
        if (blockEntity != null) return blockEntity.getInteriorNodes();
        return java.util.Collections.emptyList();
    }

    public ReactorControllerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.REACTOR_CONTROLLER.get());
    }

    private void layoutPlayerInventorySlots(Inventory playerInventory, int leftCol, int topRow) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new net.minecraft.world.inventory.Slot(playerInventory, j + i * 9 + 9, leftCol + j * 18, topRow + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new net.minecraft.world.inventory.Slot(playerInventory, i, leftCol + i * 18, topRow + 58));
        }
    }
}
