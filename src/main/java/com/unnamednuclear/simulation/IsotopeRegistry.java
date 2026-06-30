package com.unnamednuclear.simulation;

import net.minecraft.resources.ResourceLocation;
import com.unnamednuclear.UnnamedNuclear;

import java.util.*;

public class IsotopeRegistry {
    private static final Map<ResourceLocation, Isotope> ISOTOPES = new HashMap<>();

    public static void register(Isotope isotope) {
        ISOTOPES.put(isotope.id(), isotope);
    }

    public static void clear() {
        ISOTOPES.clear();
    }

    public static Isotope get(ResourceLocation id) {
        return ISOTOPES.get(id);
    }

    public static Collection<Isotope> getAll() {
        return ISOTOPES.values();
    }
}
