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

public class SolventExtractorRecipeCategory implements IRecipeCategory<SolventExtractorRecipeJEI> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "solvent_extractor");
    public static final RecipeType<SolventExtractorRecipeJEI> TYPE = new RecipeType<>(UID, SolventExtractorRecipeJEI.class);

    private final IDrawable background;
    private final IDrawable icon;

    public SolventExtractorRecipeCategory(IGuiHelper helper) {
        this.background = helper.createDrawable(ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "textures/gui/container/solvent_extractor.png"), 40, 10, 100, 65);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.SOLVENT_EXTRACTOR.get()));
    }

    @Override
    public RecipeType<SolventExtractorRecipeJEI> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.unnamednuclear.solvent_extractor");
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
    public void setRecipe(IRecipeLayoutBuilder builder, SolventExtractorRecipeJEI recipe, IFocusGroup focuses) {
        if (recipe.inputs().size() > 0) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 26).addItemStack(recipe.inputs().get(0));
        }
        if (recipe.inputs().size() > 1) {
            builder.addSlot(RecipeIngredientRole.INPUT, 5, 8).addItemStack(recipe.inputs().get(1));
        }
        
        if (recipe.outputs().size() > 0) builder.addSlot(RecipeIngredientRole.OUTPUT, 61, 26).addItemStack(recipe.outputs().get(0));
        if (recipe.outputs().size() > 1) builder.addSlot(RecipeIngredientRole.OUTPUT, 81, 26).addItemStack(recipe.outputs().get(1));
        if (recipe.outputs().size() > 2) builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 26).addItemStack(recipe.outputs().get(2));
    }
}
