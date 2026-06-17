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
            double total = comp.getTotal();
            if (total > 0) {
                addPart(tooltip, "u235", comp.u235(), total, ChatFormatting.GRAY);
                addPart(tooltip, "u238", comp.u238(), total, ChatFormatting.GRAY);
                addPart(tooltip, "u234", comp.u234(), total, ChatFormatting.DARK_GRAY);
                addPart(tooltip, "u236", comp.u236(), total, ChatFormatting.DARK_GRAY);
                addPart(tooltip, "pu239", comp.pu239(), total, ChatFormatting.GOLD);
                addPart(tooltip, "pu240", comp.pu240(), total, ChatFormatting.GOLD);
                addPart(tooltip, "sr90", comp.sr90(), total, ChatFormatting.DARK_RED);
                addPart(tooltip, "cs137", comp.cs137(), total, ChatFormatting.DARK_PURPLE);
                addPart(tooltip, "waste", comp.waste(), total, ChatFormatting.DARK_GRAY);
            }
        } else {
            Double enrichment = stack.get(Registration.ENRICHMENT.get());
            if (enrichment != null) {
                tooltip.add(Component.translatable("tooltip.unnamednuclear.enrichment", String.format("%.1f%%", enrichment * 100)).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private static void addPart(List<Component> tooltip, String key, double amount, double total, ChatFormatting color) {
        if (amount > 0.00001) {
            tooltip.add(Component.translatable("tooltip.unnamednuclear.composition." + key, String.format("%.3f%%", (amount / total) * 100)).withStyle(color));
        }
    }
}
