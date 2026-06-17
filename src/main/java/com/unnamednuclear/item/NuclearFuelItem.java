package com.unnamednuclear.item;

import com.unnamednuclear.registration.Registration;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

import java.util.List;

public class NuclearFuelItem extends Item {
    public NuclearFuelItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        TooltipUtils.appendFuelTooltip(stack, context, tooltip, flag);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
