package com.unnamednuclear.jei;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import java.util.List;

public record ChemicalConverterRecipeJEI(List<ItemStack> inputs, List<FluidStack> fluidInputs, List<ItemStack> outputs) {}
