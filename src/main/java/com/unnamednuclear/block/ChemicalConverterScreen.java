package com.unnamednuclear.block;

import com.unnamednuclear.UnnamedNuclear;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ChemicalConverterScreen extends AbstractContainerScreen<ChemicalConverterMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "textures/gui/container/chemical_converter.png");

    public ChemicalConverterScreen(ChemicalConverterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        if (menu.getMaxProgress() > 0) {
            int progress = menu.getProgress();
            int maxProgress = menu.getMaxProgress();
            int scaledProgress = (int) ((float) progress / maxProgress * 24);
            guiGraphics.blit(TEXTURE, x + 79, y + 34, 176, 0, scaledProgress, 17);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
