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
        List<ItemStack> stacks = new ArrayList<>();
        double totalU235 = 0;
        double totalU238 = 0;
        double totalPu239 = 0;
        double totalSr90 = 0;
        double totalCs137 = 0;
        double totalWaste = 0;
        double totalU234 = 0;
        double totalU236 = 0;
        double totalPu240 = 0;
        int totalItems = 0;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                NuclearComposition comp = stack.get(Registration.COMPOSITION.get());
                if (comp == null) {
                    comp = new NuclearComposition(0.0071, 0.99285, 0, 0, 0, 0, 0.00005, 0, 0);
                }
                
                int count = stack.getCount();
                totalU235 += comp.u235() * count;
                totalU238 += comp.u238() * count;
                totalPu239 += comp.pu239() * count;
                totalSr90 += comp.sr90() * count;
                totalCs137 += comp.cs137() * count;
                totalWaste += comp.waste() * count;
                totalU234 += comp.u234() * count;
                totalU236 += comp.u236() * count;
                totalPu240 += comp.pu240() * count;
                totalItems += count;
                stacks.add(stack);
            }
        }

        if (totalItems == 0) return ItemStack.EMPTY;

        NuclearComposition averageComp = new NuclearComposition(
                totalU235 / totalItems,
                totalU238 / totalItems,
                totalPu239 / totalItems,
                totalSr90 / totalItems,
                totalCs137 / totalItems,
                totalWaste / totalItems,
                totalU234 / totalItems,
                totalU236 / totalItems,
                totalPu240 / totalItems
        );

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
