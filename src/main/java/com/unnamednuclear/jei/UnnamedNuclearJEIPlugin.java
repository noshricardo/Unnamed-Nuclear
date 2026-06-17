package com.unnamednuclear.jei;

import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.item.NuclearComposition;
import com.unnamednuclear.registration.Registration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class UnnamedNuclearJEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CentrifugeRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<CentrifugeRecipeJEI> recipes = new ArrayList<>();

        // UF6 Enrichment
        ItemStack uf6 = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get(), 2);
        
        ItemStack enrichedU = new ItemStack(Registration.ENRICHED_URANIUM.get());
        enrichedU.set(Registration.COMPOSITION.get(), new NuclearComposition(0.0084, 0.9916, 0, 0, 0, 0, 0, 0, 0));
        
        ItemStack depletedU = new ItemStack(Registration.DEPLETED_URANIUM.get());
        depletedU.set(Registration.COMPOSITION.get(), new NuclearComposition(0.0056, 0.9944, 0, 0, 0, 0, 0, 0, 0));
        
        recipes.add(new CentrifugeRecipeJEI(List.of(uf6), List.of(enrichedU, depletedU)));

        // Higher Enrichment
        ItemStack enrichedInput = new ItemStack(Registration.ENRICHED_URANIUM.get(), 2);
        enrichedInput.set(Registration.COMPOSITION.get(), new NuclearComposition(0.0084, 0.9916, 0, 0, 0, 0, 0, 0, 0));

        ItemStack higherEnriched = new ItemStack(Registration.ENRICHED_URANIUM.get());
        higherEnriched.set(Registration.COMPOSITION.get(), new NuclearComposition(0.01008, 0.98992, 0, 0, 0, 0, 0, 0, 0));

        ItemStack moreDepleted = new ItemStack(Registration.DEPLETED_URANIUM.get());
        moreDepleted.set(Registration.COMPOSITION.get(), new NuclearComposition(0.00672, 0.99328, 0, 0, 0, 0, 0, 0, 0));

        recipes.add(new CentrifugeRecipeJEI(List.of(enrichedInput), List.of(higherEnriched, moreDepleted)));

        // Spent Fuel Reprocessing
        ItemStack spentFuel = new ItemStack(Registration.NUCLEAR_FUEL.get());
        spentFuel.set(Registration.COMPOSITION.get(), new NuclearComposition(0.01, 0.80, 0.05, 0.04, 0.05, 0.05, 0, 0, 0));
        
        ItemStack recoveredU = new ItemStack(Registration.ENRICHED_URANIUM.get());
        recoveredU.set(Registration.COMPOSITION.get(), new NuclearComposition(0.0123, 0.9877, 0, 0, 0, 0, 0, 0, 0)); // Normalized
        
        ItemStack recoveredPu = new ItemStack(Registration.PLUTONIUM.get());
        recoveredPu.set(Registration.COMPOSITION.get(), new NuclearComposition(0, 0, 1.0, 0, 0, 0, 0, 0, 0));
        
        ItemStack waste = new ItemStack(Registration.FISSION_PRODUCTS.get());
        
        recipes.add(new CentrifugeRecipeJEI(List.of(spentFuel), List.of(recoveredU, recoveredPu, waste)));

        registration.addRecipes(CentrifugeRecipeCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Registration.CENTRIFUGE.get()), CentrifugeRecipeCategory.TYPE);
    }
}
