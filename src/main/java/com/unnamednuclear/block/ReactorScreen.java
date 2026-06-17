package com.unnamednuclear.block;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ReactorScreen extends AbstractContainerScreen<ReactorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    public ReactorScreen(ReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Toggle"), (btn) -> {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }).pos(this.leftPos + 100, this.topPos + 35).size(60, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
        
        int y = 20;
        boolean assembled = menu.isAssembled();
        boolean active = menu.isActive();
        graphics.drawString(this.font, Component.literal("Status: " + (assembled ? "Assembled" : "Broken")), 8, y, assembled ? 0x00AA00 : 0xAA0000, false);
        y += 10;
        if (assembled) {
            graphics.drawString(this.font, Component.literal("Active: " + (active ? "Yes" : "No")), 8, y, active ? 0x00AA00 : 0xAA0000, false);
            y += 10;
        }
        y += 2;
        
        if (assembled) {
            graphics.drawString(this.font, Component.literal("Heat: " + String.format("%.2f", menu.getHeat())), 8, y, 0x404040, false);
            y += 10;
            graphics.drawString(this.font, Component.literal("Size: " + menu.getInteriorSize()), 8, y, 0x404040, false);
        } else {
            graphics.drawString(this.font, Component.translatable(menu.getLastResult().getMessageKey()), 8, y, 0xAA0000, false);
            y += 10;
            BlockPos ep = menu.getErrorPos();
            if (!ep.equals(BlockPos.ZERO)) {
                String text = "At: " + ep.getX() + ", " + ep.getY() + ", " + ep.getZ();
                int count = menu.getErrorCount();
                if (count > 1) {
                    text += " (+" + (count - 1) + " more)";
                }
                graphics.drawString(this.font, Component.literal(text), 8, y, 0xAA0000, false);
            }
        }
    }
}
