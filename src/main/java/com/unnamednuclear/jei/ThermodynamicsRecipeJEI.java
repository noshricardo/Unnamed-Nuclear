package com.unnamednuclear.jei;

import net.neoforged.neoforge.fluids.FluidStack;
import java.util.List;

public record ThermodynamicsRecipeJEI(List<FluidStack> inputs, List<FluidStack> outputs, int energyGenerated) {}
