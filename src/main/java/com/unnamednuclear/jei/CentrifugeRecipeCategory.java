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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class CentrifugeRecipeCategory implements IRecipeCategory<CentrifugeRecipeJEI> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "centrifuge");
    public static final RecipeType<CentrifugeRecipeJEI> TYPE = new RecipeType<>(UID, CentrifugeRecipeJEI.class);

    private final IDrawable background;
    private final IDrawable icon;

    public CentrifugeRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "textures/gui/container/centrifuge.png"), 40, 30, 120, 30);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.CENTRIFUGE.get()));
    }

    @Override
    public RecipeType<CentrifugeRecipeJEI> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.unnamednuclear.centrifuge");
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
    public void setRecipe(IRecipeLayoutBuilder builder, CentrifugeRecipeJEI recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 6).addIngredients(VanillaTypes.ITEM_STACK, recipe.inputs());
        
        if (recipe.outputs().size() > 0) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 6).addItemStack(recipe.outputs().get(0));
        }
        if (recipe.outputs().size() > 1) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 6).addItemStack(recipe.outputs().get(1));
        }
        if (recipe.outputs().size() > 2) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 6).addItemStack(recipe.outputs().get(2));
        }
    }
}
