package com.unnamednuclear.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class EnrichedItem extends Item {
    public EnrichedItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        TooltipUtils.appendFuelTooltip(stack, context, tooltip, flag);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
