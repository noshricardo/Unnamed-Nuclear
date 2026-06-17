package com.unnamednuclear.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ReactorType {
    String getId();
    
    void simulateNode(Level level, BlockPos pos, BlockState state, SimulationNode data);
    
    void diffuse(Level level, BlockPos pos, SimulationNode data, SimulationNode neighborData);
    
    default double getFissionRate(SimulationNode data, boolean active) {
        return active ? ((data.thermalNeutrons * 0.1) + 0.01) : 0;
    }
    
    default double getNeutronsPerFission() {
        return 2.5;
    }
    
    default double getHeatPerFission() {
        return 20.0;
    }
}
