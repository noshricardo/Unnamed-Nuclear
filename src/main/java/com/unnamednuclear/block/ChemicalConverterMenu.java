package com.unnamednuclear.block;

import com.unnamednuclear.registration.Registration;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

public class ChemicalConverterMenu extends AbstractContainerMenu {
    private final ChemicalConverterBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    public ChemicalConverterMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (ChemicalConverterBlockEntity) playerInventory.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public ChemicalConverterMenu(int containerId, Inventory playerInventory, ChemicalConverterBlockEntity blockEntity, ContainerData data) {
        super(Registration.CHEMICAL_CONVERTER_MENU.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.data = data;

        addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 44, 35));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), 1, 116, 35));
        addSlot(new SlotItemHandler(blockEntity.getInventory(), 2, 44, 17));

        layoutPlayerInventorySlots(playerInventory, 8, 84);
        addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return data.get(1);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 3) {
                if (!this.moveItemStackTo(itemstack1, 3, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, Registration.CHEMICAL_CONVERTER.get());
    }

    private void layoutPlayerInventorySlots(Inventory playerInventory, int leftCol, int topRow) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                addSlot(new Slot(playerInventory, j + i * 9 + 9, leftCol + j * 18, topRow + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            addSlot(new Slot(playerInventory, i, leftCol + i * 18, topRow + 58));
        }
    }
}
