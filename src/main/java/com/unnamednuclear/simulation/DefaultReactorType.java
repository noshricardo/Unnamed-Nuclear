package com.unnamednuclear.simulation;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultReactorType implements ReactorType {
    public static final DefaultReactorType INSTANCE = new DefaultReactorType();

    @Override
    public String getId() {
        return "default";
    }

    @Override
    public void simulateNode(Level level, BlockPos pos, BlockState state, SimulationNode data) {
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (state.is(Registration.FUEL_CHANNEL.get()) && be instanceof com.unnamednuclear.block.ReactorChannelBlockEntity channel) {
            net.minecraft.world.item.ItemStack stack = channel.getItem();
            if (stack.is(Registration.NUCLEAR_FUEL.get())) {
                com.unnamednuclear.item.NuclearComposition comp = stack.get(Registration.COMPOSITION.get());
                if (comp == null) {
                    // Fallback to legacy enrichment or default
                    double enrichment = stack.getOrDefault(Registration.ENRICHMENT.get(), 0.02);
                    comp = new com.unnamednuclear.item.NuclearComposition(enrichment, 1.0 - enrichment, 0, 0);
                    stack.set(Registration.COMPOSITION.get(), comp);
                }

                if (comp.getTotal() > 0) {
                    // Fission
                    double fissionU235 = (data.thermalNeutrons * 0.5 * comp.u235()) + (0.001 * comp.u235());
                    double fissionPu239 = (data.thermalNeutrons * 0.8 * comp.pu239()) + (0.001 * comp.pu239());
                    double totalFission = fissionU235 + fissionPu239;
                    
                    data.nextFastNeutrons += totalFission * getNeutronsPerFission();
                    data.nextHeat += totalFission * getHeatPerFission();

                    // Breeding
                    double breeding = (data.fastNeutrons * 0.2 * comp.u238());
                    data.nextFastNeutrons -= breeding; // Neutrons captured

                    // Absorption by waste (poisoning)
                    double poison = data.thermalNeutrons * 0.1 * comp.waste();
                    data.nextThermalNeutrons -= poison;

                    // Chemical Changes (slow)
                    if (level.getRandom().nextDouble() < 0.1) {
                        double deltaU235 = fissionU235 * 0.01;
                        double deltaPu239 = fissionPu239 * 0.01 - breeding * 0.01;
                        double deltaU238 = breeding * 0.01;
                        double deltaWaste = totalFission * 0.01;

                        double newU235 = Math.max(0, comp.u235() - deltaU235);
                        double newU238 = Math.max(0, comp.u238() - deltaU238);
                        double newPu239 = Math.max(0, comp.pu239() - deltaPu239);
                        double newWaste = comp.waste() + deltaWaste;

                        stack.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(newU235, newU238, newPu239, newWaste));
                        channel.setChanged();
                    }
                }
            }
        } else if (state.is(Registration.MODERATOR.get())) {
            double moderated = data.fastNeutrons * 0.5;
            data.nextFastNeutrons -= moderated;
            data.nextThermalNeutrons += moderated;
        } else if (state.is(Registration.CONTROL_CHANNEL.get()) && be instanceof com.unnamednuclear.block.ReactorChannelBlockEntity channel) {
            if (channel.getItem().is(Registration.CONTROL_ROD_ITEM.get())) {
                data.nextFastNeutrons -= data.fastNeutrons * 0.9;
                data.nextThermalNeutrons -= data.thermalNeutrons * 0.9;
            }
        } else if (state.is(Registration.COOLANT_CHANNEL.get())) {
            data.nextHeat -= data.heat * 0.2;
        }
        
        // Ambient cooling
        data.nextHeat -= data.heat * 0.005;
    }

    @Override
    public void diffuse(Level level, BlockPos pos, SimulationNode data, SimulationNode neighborData) {
        double diffusedFast = data.fastNeutrons * 0.1;
        double diffusedThermal = data.thermalNeutrons * 0.1;
        double diffusedHeat = data.heat * 0.05;

        data.nextFastNeutrons -= diffusedFast;
        data.nextThermalNeutrons -= diffusedThermal;
        data.nextHeat -= diffusedHeat;

        if (neighborData != null) {
            neighborData.nextFastNeutrons += diffusedFast;
            neighborData.nextThermalNeutrons += diffusedThermal;
            neighborData.nextHeat += diffusedHeat;
        } else {
            // Diffusion into non-simulation blocks (loss)
            // If it's a casing, we might want to track heat in the casing too, 
            // but for now let's just let it dissipate.
        }
    }
}
