package com.unnamednuclear.recipe;

import com.unnamednuclear.item.NuclearComposition;
import com.unnamednuclear.registration.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class UraniumHexafluorideMixingRecipe extends CustomRecipe {
    public UraniumHexafluorideMixingRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int count = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (!stack.is(Registration.URANIUM_HEXAFLUORIDE.get())) {
                    return false;
                }
                count++;
            }
        }
        return count >= 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        java.util.Map<net.minecraft.resources.ResourceLocation, Double> totalAmounts = new java.util.HashMap<>();
        int totalItems = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                NuclearComposition comp = stack.get(Registration.COMPOSITION.get());
                if (comp == null) {
                    comp = new NuclearComposition(java.util.Map.of(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unnamednuclear", "u235"), 0.0071,
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unnamednuclear", "u238"), 0.99285,
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("unnamednuclear", "u234"), 0.00005
                    ));
                }
                
                int count = stack.getCount();
                for (java.util.Map.Entry<net.minecraft.resources.ResourceLocation, Double> entry : comp.amounts().entrySet()) {
                    totalAmounts.put(entry.getKey(), totalAmounts.getOrDefault(entry.getKey(), 0.0) + entry.getValue() * count);
                }
                totalItems += count;
            }
        }

        if (totalItems == 0) return ItemStack.EMPTY;

        final double finalTotalItems = totalItems;
        java.util.Map<net.minecraft.resources.ResourceLocation, Double> averageAmounts = new java.util.HashMap<>();
        totalAmounts.forEach((id, amount) -> averageAmounts.put(id, amount / finalTotalItems));

        NuclearComposition averageComp = new NuclearComposition(averageAmounts);

        ItemStack result = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get(), 1);
        if (totalItems > 16) {
             result.setCount(16);
        } else {
             result.setCount(totalItems);
        }
        result.set(Registration.COMPOSITION.get(), averageComp);
        return result;
    }

    @Override
    public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        net.minecraft.core.NonNullList<ItemStack> remaining = net.minecraft.core.NonNullList.withSize(input.size(), ItemStack.EMPTY);
        int totalItems = 0;
        for (int i = 0; i < input.size(); i++) {
            totalItems += input.getItem(i).getCount();
        }

        if (totalItems <= 16) {
            return remaining;
        }

        int toReturn = totalItems - 16;
        for (int i = 0; i < input.size() && toReturn > 0; i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                int take = Math.min(stack.getCount(), toReturn);
                ItemStack returned = stack.copy();
                returned.setCount(take);
                remaining.set(i, returned);
                toReturn -= take;
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.UF6_MIXING_SERIALIZER.get();
    }
}
