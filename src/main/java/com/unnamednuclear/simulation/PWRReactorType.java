package com.unnamednuclear.simulation;

import com.unnamednuclear.registration.Registration;
import net.minecraft.world.level.block.state.BlockState;

public class PWRReactorType extends DefaultReactorType {
    public static final PWRReactorType INSTANCE = new PWRReactorType();

    @Override
    public String getId() {
        return "pwr";
    }

    @Override
    public double getModerationEfficiency(BlockState state, SimulationNode data) {
        if (state.is(Registration.COOLANT_CHANNEL.get())) {
            return 0.95; // Water in coolant channels acts as moderator
        }
        return 0;
    }
    
    @Override
    public double getVoidCoefficient() {
        return -0.2; // Negative void coefficient (inherently stable)
    }

    @Override
    public double getTemperatureCoefficient() {
        return -0.0005; // Strong negative temperature coefficient
    }
    
    @Override
    protected double getModerationLoss(BlockState state) {
        return 0.05; // Water absorbs more than graphite
    }
}
