package com.unnamednuclear.simulation;

import com.unnamednuclear.registration.Registration;
import net.minecraft.world.level.block.state.BlockState;

public class RBMKReactorType extends DefaultReactorType {
    public static final RBMKReactorType INSTANCE = new RBMKReactorType();

    @Override
    public String getId() {
        return "rbmk";
    }

    @Override
    public double getModerationEfficiency(BlockState state, SimulationNode data) {
        if (state.is(Registration.MODERATOR.get())) {
            return 0.85; // Graphite in RBMK
        }
        return 0;
    }

    @Override
    public double getVoidCoefficient() {
        return 0.4; // Strong positive void coefficient (Chernobyl flaw)
    }

    @Override
    public double getTemperatureCoefficient() {
        return 0.0001; // Slightly positive or neutral at some ranges
    }
}
