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
import java.util.List;

public class ThermodynamicsRecipeCategory implements IRecipeCategory<ThermodynamicsRecipeJEI> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "thermodynamics");
    public static final RecipeType<ThermodynamicsRecipeJEI> TYPE = new RecipeType<>(UID, ThermodynamicsRecipeJEI.class);

    private final IDrawable background;
    private final IDrawable icon;

    public ThermodynamicsRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(120, 50);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Registration.HEAT_EXCHANGER.get()));
    }

    @Override
    public RecipeType<ThermodynamicsRecipeJEI> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Thermodynamics");
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
    public void setRecipe(IRecipeLayoutBuilder builder, ThermodynamicsRecipeJEI recipe, IFocusGroup focuses) {
        int x = 5;
        for (int i = 0; i < recipe.inputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, x + i * 20, 5).addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.inputs().get(i)));
        }
        
        x = 60;
        for (int i = 0; i < recipe.outputs().size(); i++) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, x + i * 20, 5).addIngredients(NeoForgeTypes.FLUID_STACK, List.of(recipe.outputs().get(i)));
        }
        
        if (recipe.energyGenerated() > 0) {
            // No easy way to show FE in basic category without custom drawable, but we can add a text or something
        }
    }
}
