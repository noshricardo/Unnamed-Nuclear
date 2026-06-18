package com.unnamednuclear.jei;

import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.registration.Registration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class ChemicalConverterRecipeCategory implements IRecipeCategory<ChemicalConverterRecipeJEI> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "chemical_converter");
    public static final RecipeType<ChemicalConverterRecipeJEI> TYPE = new RecipeType<>(UID, ChemicalConverterRecipeJEI.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ChemicalConverterRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "textures/gui/container/chemical_converter.png"), 40, 30, 120, 30);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.CHEMICAL_CONVERTER.get()));
    }

    @Override
    public RecipeType<ChemicalConverterRecipeJEI> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.unnamednuclear.chemical_converter");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ChemicalConverterRecipeJEI recipe, IFocusGroup focuses) {
        if (recipe.inputs().size() > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 6).addItemStack(recipe.inputs().get(0));
        }
        if (recipe.inputs().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, -12).addItemStack(recipe.inputs().get(1));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 6).addIngredients(VanillaTypes.ITEM_STACK, recipe.outputs());
    }
}
