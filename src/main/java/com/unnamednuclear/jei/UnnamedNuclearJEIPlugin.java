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

import mezz.jei.api.neoforge.NeoForgeTypes;
import net.neoforged.neoforge.fluids.FluidStack;

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
        registration.addRecipeCategories(new ChemicalConverterRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new SolventExtractorRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ThermodynamicsRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registerCentrifugeRecipes(registration);
        registerConverterRecipes(registration);
        registerExtractorRecipes(registration);
        registerThermodynamicsRecipes(registration);
    }

    private void registerCentrifugeRecipes(IRecipeRegistration registration) {
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

    private void registerConverterRecipes(IRecipeRegistration registration) {
        List<ChemicalConverterRecipeJEI> recipes = new ArrayList<>();
        
        // Yellowcake -> Uranyl Nitrate
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.YELLOWCAKE.get())),
            List.of(new FluidStack(Registration.HNO3.get(), 200)),
            List.of(new ItemStack(Registration.URANYL_NITRATE.get()))
        ));
        
        // Uranyl Nitrate -> UO2
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.URANYL_NITRATE.get())),
            List.of(),
            List.of(new ItemStack(Registration.URANIUM_DIOXIDE.get()))
        ));

        // UO2 -> UF4
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.URANIUM_DIOXIDE.get())),
            List.of(new FluidStack(Registration.HF.get(), 400)),
            List.of(new ItemStack(Registration.URANIUM_TETRAFLUORIDE.get()))
        ));

        // UF4 -> UF6
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.URANIUM_TETRAFLUORIDE.get())),
            List.of(new FluidStack(Registration.F2.get(), 200)),
            List.of(new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get()))
        ));

        registration.addRecipes(ChemicalConverterRecipeCategory.TYPE, recipes);
    }

    private void registerExtractorRecipes(IRecipeRegistration registration) {
        List<SolventExtractorRecipeJEI> recipes = new ArrayList<>();
        
        // PUREX Dissolution/Extraction
        ItemStack spentFuel = new ItemStack(Registration.NUCLEAR_FUEL.get());
        spentFuel.set(Registration.COMPOSITION.get(), new NuclearComposition(0.01, 0.80, 0.05, 0.04, 0.05, 0.05, 0, 0, 0));
        
        recipes.add(new SolventExtractorRecipeJEI(
            List.of(spentFuel),
            List.of(new FluidStack(Registration.HNO3.get(), 1000)),
            List.of(new ItemStack(Registration.URANYL_NITRATE.get()), new ItemStack(Registration.PLUTONIUM_NITRATE.get()), new ItemStack(Registration.FISSION_PRODUCTS.get()))
        ));

        // PUREX Extraction: Uranyl Nitrate + TBP -> UO2 (Separation)
        ItemStack uranylNitrate = new ItemStack(Registration.URANYL_NITRATE.get());
        recipes.add(new SolventExtractorRecipeJEI(
            List.of(uranylNitrate),
            List.of(new FluidStack(Registration.TBP.get(), 500)),
            List.of(new ItemStack(Registration.URANIUM_DIOXIDE.get()))
        ));

        // PUREX Extraction: Plutonium Nitrate + TBP -> Plutonium
        ItemStack plutoniumNitrate = new ItemStack(Registration.PLUTONIUM_NITRATE.get());
        recipes.add(new SolventExtractorRecipeJEI(
            List.of(plutoniumNitrate),
            List.of(new FluidStack(Registration.TBP.get(), 500)),
            List.of(new ItemStack(Registration.PLUTONIUM.get()))
        ));

        registration.addRecipes(SolventExtractorRecipeCategory.TYPE, recipes);
    }

    private void registerThermodynamicsRecipes(IRecipeRegistration registration) {
        List<ThermodynamicsRecipeJEI> recipes = new ArrayList<>();

        // Heat Exchanger: Hot Sodium + Water -> Sodium + Steam
        recipes.add(new ThermodynamicsRecipeJEI(
            List.of(new FluidStack(Registration.HOT_SODIUM.get(), 1000), new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000)),
            List.of(new FluidStack(Registration.SODIUM.get(), 1000), new FluidStack(Registration.STEAM.get(), 1000)),
            0
        ));

        // Steam Turbine: Steam -> Water + Energy
        recipes.add(new ThermodynamicsRecipeJEI(
            List.of(new FluidStack(Registration.STEAM.get(), 1000)),
            List.of(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000)),
            10000 // 10k FE per bucket of steam (example)
        ));

        registration.addRecipes(ThermodynamicsRecipeCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Registration.CENTRIFUGE.get()), CentrifugeRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Registration.CHEMICAL_CONVERTER.get()), ChemicalConverterRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Registration.SOLVENT_EXTRACTOR.get()), SolventExtractorRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Registration.HEAT_EXCHANGER.get()), ThermodynamicsRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(Registration.STEAM_TURBINE.get()), ThermodynamicsRecipeCategory.TYPE);
    }
}
