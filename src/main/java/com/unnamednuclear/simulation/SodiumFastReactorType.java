package com.unnamednuclear.simulation;

import com.unnamednuclear.registration.Registration;
import net.minecraft.world.level.block.state.BlockState;

public class SodiumFastReactorType extends DefaultReactorType {
    public static final SodiumFastReactorType INSTANCE = new SodiumFastReactorType();

    @Override
    public String getId() {
        return "sodium_fast";
    }

    @Override
    public double getModerationEfficiency(BlockState state, SimulationNode data) {
        return 0; // No moderation in fast reactor
    }

    @Override
    public double getFissionRate(SimulationNode data, boolean active) {
        // Fast fission requires more neutrons or different cross-section
        return active ? ((data.fastNeutrons * 0.15) + (data.thermalNeutrons * 0.05) + 0.01) : 0;
    }

    @Override
    public double getNeutronsPerFission() {
        return 2.9; // Higher in fast spectrum
    }
}
