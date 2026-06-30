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
                for (java.util.Map.Entry<net.minecraft.resources.ResourceLocation, Double> entry : comp.amounts().entrySet()) {
                    addPart(tooltip, entry.getKey().getPath(), entry.getValue(), total, ChatFormatting.GRAY);
                }
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
