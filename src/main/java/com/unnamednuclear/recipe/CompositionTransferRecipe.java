package com.unnamednuclear.recipe;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public class CompositionTransferRecipe extends CustomRecipe {
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public CompositionTransferRecipe(CraftingBookCategory category, NonNullList<Ingredient> ingredients, ItemStack result) {
        super(category);
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                inputs.add(stack);
            }
        }

        if (inputs.size() != ingredients.size()) {
            return false;
        }

        java.util.List<ItemStack> remainingInputs = new java.util.ArrayList<>(inputs);
        for (Ingredient ingredient : ingredients) {
            boolean found = false;
            for (int i = 0; i < remainingInputs.size(); i++) {
                if (ingredient.test(remainingInputs.get(i))) {
                    remainingInputs.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return remainingInputs.isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack output = result.copy();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                // Transfer from the first ingredient that has composition/enrichment
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
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= ingredients.size();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Registration.COMPOSITION_TRANSFER_SERIALIZER.get();
    }

    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result;
    }
}
