package com.unnamednuclear.simulation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.unnamednuclear.UnnamedNuclear;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class IsotopeDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public IsotopeDataLoader() {
        super(GSON, "isotopes");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        UnnamedNuclear.LOGGER.info("Loading isotopes...");
        IsotopeRegistry.clear();
        int count = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                Isotope isotope = Isotope.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                        .getOrThrow(s -> new RuntimeException("Failed to parse isotope " + id + ": " + s));
                
                // If the isotope ID in the JSON doesn't match the file name, we might want to override it
                // or just trust the JSON. Usually for data-driven things, the file path is the ID.
                // Isotope record has an 'id' field.
                
                IsotopeRegistry.register(isotope);
                count++;
            } catch (Exception e) {
                UnnamedNuclear.LOGGER.error("Failed to load isotope {}: {}", id, e.getMessage());
            }
        }
        UnnamedNuclear.LOGGER.info("Loaded {} isotopes", count);
    }
}
