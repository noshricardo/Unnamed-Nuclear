package com.unnamednuclear.simulation;

public class SimulationNode {
    public double fastNeutrons;
    public double thermalNeutrons;
    public double heat;
    public double iodine135;
    public double xenon135;
    
    public double nextFastNeutrons;
    public double nextThermalNeutrons;
    public double nextHeat;
    public double nextIodine135;
    public double nextXenon135;

    public void update() {
        fastNeutrons = Math.max(0, fastNeutrons + nextFastNeutrons);
        thermalNeutrons = Math.max(0, thermalNeutrons + nextThermalNeutrons);
        heat = Math.max(0, heat + nextHeat);
        iodine135 = Math.max(0, iodine135 + nextIodine135);
        xenon135 = Math.max(0, xenon135 + nextXenon135);
        
        nextFastNeutrons = 0;
        nextThermalNeutrons = 0;
        nextHeat = 0;
        nextIodine135 = 0;
        nextXenon135 = 0;
    }
}
