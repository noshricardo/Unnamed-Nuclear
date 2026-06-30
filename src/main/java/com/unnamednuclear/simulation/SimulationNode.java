package com.unnamednuclear.simulation;

import com.unnamednuclear.item.NuclearComposition;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class SimulationNode {
    public double fastNeutrons;
    public double thermalNeutrons;
    public double heat;
    public double emissions;
    public NuclearComposition composition = NuclearComposition.EMPTY;
    
    public double nextFastNeutrons;
    public double nextThermalNeutrons;
    public double nextHeat;
    public double nextEmissions;
    public Map<ResourceLocation, Double> nextComposition = new HashMap<>();

    // Simulation parameters
    public double fissionYield = 0.8;
    public double neutronsPerFission = 2.5;
    public double heatPerFission = 20.0;
    public double moderationEfficiency = 0.0;
    public double moderationLoss = 0.02;
    public double voidCoefficient = 0.0;
    public double temperatureCoefficient = -0.0001;

    public void update() {
        fastNeutrons = Math.max(0, fastNeutrons + nextFastNeutrons);
        thermalNeutrons = Math.max(0, thermalNeutrons + nextThermalNeutrons);
        heat = Math.max(0, heat + nextHeat);
        emissions = nextEmissions;
        
        if (!nextComposition.isEmpty()) {
            Map<ResourceLocation, Double> newAmounts = new HashMap<>(composition.amounts());
            for (Map.Entry<ResourceLocation, Double> entry : nextComposition.entrySet()) {
                double newAmount = newAmounts.getOrDefault(entry.getKey(), 0.0) + entry.getValue();
                if (newAmount <= 0) {
                    newAmounts.remove(entry.getKey());
                } else {
                    newAmounts.put(entry.getKey(), newAmount);
                }
            }
            composition = new NuclearComposition(newMap(newAmounts));
            nextComposition.clear();
        }
        
        nextFastNeutrons = 0;
        nextThermalNeutrons = 0;
        nextHeat = 0;
        nextEmissions = 0;
    }

    private Map<ResourceLocation, Double> newMap(Map<ResourceLocation, Double> map) {
        return map; // NuclearComposition constructor handles it, but ensuring it's not the same reference if needed
    }
}
