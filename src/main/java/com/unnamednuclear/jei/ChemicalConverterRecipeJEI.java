package com.unnamednuclear.jei;

import net.minecraft.world.item.ItemStack;
import java.util.List;

public record ChemicalConverterRecipeJEI(List<ItemStack> inputs, List<ItemStack> outputs) {}
