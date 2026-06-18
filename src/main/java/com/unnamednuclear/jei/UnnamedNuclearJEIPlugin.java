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

        // UF6 Enrichment (Natural -> LEU)
        ItemStack uf6 = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        uf6.set(Registration.COMPOSITION.get(), new NuclearComposition(0.0071, 0.9929, 0, 0, 0, 0, 0, 0, 0));
        
        ItemStack enrichedU = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        enrichedU.set(Registration.COMPOSITION.get(), new NuclearComposition(0.03, 0.97, 0, 0, 0, 0, 0, 0, 0));
        
        ItemStack depletedU = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        depletedU.set(Registration.COMPOSITION.get(), new NuclearComposition(0.002, 0.998, 0, 0, 0, 0, 0, 0, 0));
        
        recipes.add(new CentrifugeRecipeJEI(List.of(uf6), List.of(enrichedU, depletedU)));

        // Higher Enrichment (LEU -> HEU)
        ItemStack leuInput = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        leuInput.set(Registration.COMPOSITION.get(), new NuclearComposition(0.03, 0.97, 0, 0, 0, 0, 0, 0, 0));

        ItemStack heuOutput = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        heuOutput.set(Registration.COMPOSITION.get(), new NuclearComposition(0.20, 0.80, 0, 0, 0, 0, 0, 0, 0));

        ItemStack tailsOutput = new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get());
        tailsOutput.set(Registration.COMPOSITION.get(), new NuclearComposition(0.01, 0.99, 0, 0, 0, 0, 0, 0, 0));

        recipes.add(new CentrifugeRecipeJEI(List.of(leuInput), List.of(heuOutput, tailsOutput)));

        registration.addRecipes(CentrifugeRecipeCategory.TYPE, recipes);
    }

    private void registerConverterRecipes(IRecipeRegistration registration) {
        List<ChemicalConverterRecipeJEI> recipes = new ArrayList<>();
        
        // Yellowcake + Nitric Acid -> Uranyl Nitrate
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.YELLOWCAKE.get()), new ItemStack(Registration.NITRIC_ACID.get())),
            List.of(new ItemStack(Registration.URANYL_NITRATE.get()))
        ));
        
        // Uranyl Nitrate -> UO2
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.URANYL_NITRATE.get())),
            List.of(new ItemStack(Registration.URANIUM_DIOXIDE.get()))
        ));

        // UO2 + HF -> UF4
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.URANIUM_DIOXIDE.get()), new ItemStack(Registration.HYDROFLUORIC_ACID.get())),
            List.of(new ItemStack(Registration.URANIUM_TETRAFLUORIDE.get()))
        ));

        // UF4 + F2 -> UF6
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.URANIUM_TETRAFLUORIDE.get()), new ItemStack(Registration.FLUORINE_GAS.get())),
            List.of(new ItemStack(Registration.URANIUM_HEXAFLUORIDE.get()))
        ));
        
        // HF -> F2
        recipes.add(new ChemicalConverterRecipeJEI(
            List.of(new ItemStack(Registration.HYDROFLUORIC_ACID.get())),
            List.of(new ItemStack(Registration.FLUORINE_GAS.get()))
        ));

        registration.addRecipes(ChemicalConverterRecipeCategory.TYPE, recipes);
    }

    private void registerExtractorRecipes(IRecipeRegistration registration) {
        List<SolventExtractorRecipeJEI> recipes = new ArrayList<>();
        
        // PUREX Dissolution/Extraction: Spent Fuel + Nitric Acid -> Nitrates
        ItemStack spentFuel = new ItemStack(Registration.NUCLEAR_FUEL.get());
        spentFuel.set(Registration.COMPOSITION.get(), new NuclearComposition(0.01, 0.80, 0.05, 0.04, 0.05, 0.05, 0, 0, 0));
        
        recipes.add(new SolventExtractorRecipeJEI(
            List.of(spentFuel, new ItemStack(Registration.NITRIC_ACID.get())),
            List.of(new ItemStack(Registration.URANYL_NITRATE.get()), new ItemStack(Registration.PLUTONIUM_NITRATE.get()), new ItemStack(Registration.FISSION_PRODUCTS.get()))
        ));

        // PUREX Extraction: Uranyl Nitrate + TBP -> UO2 (Separation)
        ItemStack uranylNitrate = new ItemStack(Registration.URANYL_NITRATE.get());
        recipes.add(new SolventExtractorRecipeJEI(
            List.of(uranylNitrate, new ItemStack(Registration.TBP_KEROSENE.get())),
            List.of(new ItemStack(Registration.URANIUM_DIOXIDE.get()))
        ));

        // PUREX Extraction: Plutonium Nitrate + TBP -> Plutonium
        ItemStack plutoniumNitrate = new ItemStack(Registration.PLUTONIUM_NITRATE.get());
        recipes.add(new SolventExtractorRecipeJEI(
            List.of(plutoniumNitrate, new ItemStack(Registration.TBP_KEROSENE.get())),
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
