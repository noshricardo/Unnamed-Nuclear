package com.unnamednuclear.recipe;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import net.minecraft.world.item.crafting.ShapedRecipePattern;

public class ShapedCompositionTransferRecipe extends ShapedRecipe {
    public ShapedCompositionTransferRecipe(ShapedRecipe compose) {
        super(compose.getGroup(), compose.category(), compose.pattern, compose.getResultItem(null));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack output = super.assemble(input, registries);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                if (stack.has(Registration.COMPOSITION.get()) || stack.has(Registration.ENRICHMENT.get())) {
                    if (stack.has(Registration.COMPOSITION.get())) {
                        output.set(Registration.COMPOSITION.get(), stack.get(Registration.COMPOSITION.get()));
                    }
                    if (stack.has(Registration.FUEL_TYPE.get())) {
                        output.set(Registration.FUEL_TYPE.get(), stack.get(Registration.FUEL_TYPE.get()));
                    }
                    if (stack.has(Registration.ENRICHMENT.get())) {
                        output.set(Registration.ENRICHMENT.get(), stack.get(Registration.ENRICHMENT.get()));
                    }
                    break;
                }
            }
        }
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.SHAPED_COMPOSITION_TRANSFER_SERIALIZER.get();
    }
}
