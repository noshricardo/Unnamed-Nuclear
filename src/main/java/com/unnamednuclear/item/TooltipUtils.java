package com.unnamednuclear.item;

import com.unnamednuclear.registration.Registration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;

import java.util.List;

public class TooltipUtils {
    public static void appendFuelTooltip(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        String type = stack.get(Registration.FUEL_TYPE.get());
        NuclearComposition comp = stack.get(Registration.COMPOSITION.get());

        if (type != null) {
            tooltip.add(Component.translatable("tooltip.unnamednuclear.fuel_type", type).withStyle(ChatFormatting.BLUE));
        }

        if (comp != null) {
            double u235 = comp.u235();
            double u238 = comp.u238();
            double pu239 = comp.pu239();
            double waste = comp.waste();
            double total = comp.getTotal();

            if (total > 0) {
                tooltip.add(Component.translatable("tooltip.unnamednuclear.composition.u235", String.format("%.2f%%", (u235 / total) * 100)).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.unnamednuclear.composition.u238", String.format("%.2f%%", (u238 / total) * 100)).withStyle(ChatFormatting.GRAY));
                if (pu239 > 0.0001) {
                    tooltip.add(Component.translatable("tooltip.unnamednuclear.composition.pu239", String.format("%.2f%%", (pu239 / total) * 100)).withStyle(ChatFormatting.GOLD));
                }
                if (waste > 0.0001) {
                    tooltip.add(Component.translatable("tooltip.unnamednuclear.composition.waste", String.format("%.2f%%", (waste / total) * 100)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
        } else {
            Double enrichment = stack.get(Registration.ENRICHMENT.get());
            if (enrichment != null) {
                tooltip.add(Component.translatable("tooltip.unnamednuclear.enrichment", String.format("%.1f%%", enrichment * 100)).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
