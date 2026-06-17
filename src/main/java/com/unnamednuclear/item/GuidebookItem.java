package com.unnamednuclear.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuidebookItem extends Item {
    public GuidebookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            vazkii.patchouli.api.PatchouliAPI.get().openBookGUI(serverPlayer, ResourceLocation.fromNamespaceAndPath("unnamednuclear", "guidebook"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
