package com.unnamednuclear.block;

import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.client.ClientReactorData;
import com.unnamednuclear.client.ClientSimulationData;
import com.unnamednuclear.network.ReactorInteriorSyncPayload;
import com.unnamednuclear.network.SimulationDataSyncPayload;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ReactorScreen extends AbstractContainerScreen<ReactorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "textures/gui/container/reactor_controller.png");
    private BlockPos selectedPos = null;
    private int selectedIndex = -1;

    public ReactorScreen(ReactorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 220;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.addRenderableWidget(Button.builder(Component.literal("Toggle"), (btn) -> {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }).pos(this.leftPos + 110, this.topPos + 10).size(50, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Extract"), (btn) -> {
            if (selectedIndex != -1) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 100 + (0 * 10000) + selectedIndex);
            }
        }).pos(this.leftPos + 180, this.topPos + 40).size(60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Insert"), (btn) -> {
            if (selectedIndex != -1) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 100 + (1 * 10000) + selectedIndex);
            }
        }).pos(this.leftPos + 180, this.topPos + 65).size(60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Rod"), (btn) -> {
            if (selectedIndex != -1) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 100 + (2 * 10000) + selectedIndex);
            }
        }).pos(this.leftPos + 180, this.topPos + 90).size(60, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderLocality(graphics, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderLocality(GuiGraphics graphics, int mouseX, int mouseY) {
        List<ReactorInteriorSyncPayload.ChannelData> channels = ClientReactorData.getChannels();
        if (channels.isEmpty()) return;

        int gridX = this.leftPos + 8;
        int gridY = this.topPos + 80;
        int cellSize = 10;

        // Find bounds
        int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
        int fixedY = channels.get(0).pos().getY();

        for (ReactorInteriorSyncPayload.ChannelData data : channels) {
            if (data.pos().getY() != fixedY) continue; // Show only one layer for now
            minX = Math.min(minX, data.pos().getX());
            minZ = Math.min(minZ, data.pos().getZ());
            maxX = Math.max(maxX, data.pos().getX());
            maxZ = Math.max(maxZ, data.pos().getZ());
        }

        for (int i = 0; i < channels.size(); i++) {
            ReactorInteriorSyncPayload.ChannelData data = channels.get(i);
            if (data.pos().getY() != fixedY) continue;

            int x = gridX + (data.pos().getX() - minX) * cellSize;
            int z = gridY + (data.pos().getZ() - minZ) * cellSize;

            // Determine color based on flux
            int color = 0xFFAAAAAA; // Default gray
            SimulationDataSyncPayload.NodeData nodeData = ClientSimulationData.getNodes().get(data.pos());
            if (nodeData != null) {
                float flux = nodeData.thermal() + nodeData.fast();
                if (flux > 0) {
                    int r = (int) Math.min(255, flux * 10);
                    int g = (int) Math.max(0, 255 - flux * 10);
                    color = 0xFF000000 | (r << 16) | (g << 8);
                }
            }
            
            // Mark special types
            if (data.type().equals("fuel")) {
                 graphics.fill(x + 1, z + 1, x + cellSize - 1, z + cellSize - 1, 0xFFFFFF00); // Yellow border for fuel
            }

            graphics.fill(x + 2, z + 2, x + cellSize - 2, z + cellSize - 2, color);
            
            if (data.pos().equals(selectedPos)) {
                graphics.renderOutline(x, z, cellSize, cellSize, 0xFFFFFFFF);
            }

            if (mouseX >= x && mouseX < x + cellSize && mouseY >= z && mouseY < z + cellSize) {
                if (this.minecraft.mouseHandler.isLeftPressed()) {
                    selectedPos = data.pos();
                    selectedIndex = i;
                }
            }
        }
        
        if (selectedPos != null && selectedIndex != -1) {
            ReactorInteriorSyncPayload.ChannelData data = channels.get(selectedIndex);
            int infoX = this.leftPos + 180;
            int infoY = this.topPos + 120;
            graphics.drawString(this.font, "Type: " + data.type(), infoX, infoY, 0x404040, false);
            graphics.drawString(this.font, "Ins: " + data.insertion(), infoX, infoY + 10, 0x404040, false);
            if (!data.item().isEmpty()) {
                graphics.renderItem(data.item(), infoX, infoY + 25);
            }
        }
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
            graphics.drawString(this.font, Component.literal("Flux: " + String.format("%.2f", menu.getFlux())), 8, y, 0x404040, false);
            y += 10;
            graphics.drawString(this.font, Component.literal("Xenon: " + String.format("%.6f", menu.getXenon())), 8, y, 0x404040, false);
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
