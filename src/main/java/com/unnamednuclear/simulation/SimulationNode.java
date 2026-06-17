package com.unnamednuclear.simulation;

public class SimulationNode {
    public double fastNeutrons;
    public double thermalNeutrons;
    public double heat;
    
    public double nextFastNeutrons;
    public double nextThermalNeutrons;
    public double nextHeat;

    public void update() {
        fastNeutrons = Math.max(0, fastNeutrons + nextFastNeutrons);
        thermalNeutrons = Math.max(0, thermalNeutrons + nextThermalNeutrons);
        heat = Math.max(0, heat + nextHeat);
        
        nextFastNeutrons = 0;
        nextThermalNeutrons = 0;
        nextHeat = 0;
    }
}
