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
                    double enrichment = stack.getOrDefault(Registration.ENRICHMENT.get(), 0.02);
                    comp = new com.unnamednuclear.item.NuclearComposition(enrichment, 1.0 - enrichment, 0, 0, 0, 0, 0, 0, 0);
                    stack.set(Registration.COMPOSITION.get(), comp);
                }

                if (comp.getTotal() > 0) {
                    // Fission
                    // Thermal neutrons are absorbed by fuel to cause fission.
                    // Xenon-135 is a major neutron poison.
                    double xenonAbsorption = data.thermalNeutrons * 0.8 * (data.xenon135 / (data.xenon135 + 0.1));
                    double availableThermalNeutrons = Math.max(0, data.thermalNeutrons - xenonAbsorption);
                    data.nextThermalNeutrons -= xenonAbsorption;

                    double fissionU235 = (availableThermalNeutrons * 0.5 * comp.u235()) + (0.001 * comp.u235());
                    double fissionPu239 = (availableThermalNeutrons * 0.8 * comp.pu239()) + (0.001 * comp.pu239());
                    double totalFission = fissionU235 + fissionPu239;
                    
                    data.nextFastNeutrons += totalFission * getNeutronsPerFission();
                    data.nextHeat += totalFission * getHeatPerFission();
                    data.nextThermalNeutrons -= (fissionU235 + fissionPu239); // Absorbed for fission

                    // Iodine and Xenon production
                    data.nextIodine135 += totalFission * 0.06; // 6% yield
                    
                    // Breeding
                    double breeding = (data.fastNeutrons * 0.2 * comp.u238());
                    data.nextFastNeutrons -= breeding; // Neutrons captured

                    // Absorption by waste (poisoning)
                    double poison = availableThermalNeutrons * 0.1 * comp.waste();
                    data.nextThermalNeutrons -= poison;

                    // Chemical Changes (slow)
                    if (level.getRandom().nextDouble() < 0.1) {
                        double deltaU235 = fissionU235 * 0.01;
                        double deltaPu239 = (fissionPu239 * 0.01) - (breeding * 0.01);
                        double deltaU238 = breeding * 0.01;
                        double deltaSr90 = totalFission * 0.004;
                        double deltaCs137 = totalFission * 0.005;
                        double deltaWaste = totalFission * 0.001;

                        double newU235 = Math.max(0, comp.u235() - deltaU235);
                        double newU238 = Math.max(0, comp.u238() - deltaU238);
                        double newPu239 = Math.max(0, comp.pu239() - deltaPu239);
                        double newSr90 = comp.sr90() + deltaSr90;
                        double newCs137 = comp.cs137() + deltaCs137;
                        double newWaste = comp.waste() + deltaWaste;

                        stack.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(newU235, newU238, newPu239, newSr90, newCs137, newWaste, comp.u234(), comp.u236(), comp.pu240()));
                        channel.setChanged();
                    }
                }
            }
        } else if (state.is(Registration.MODERATOR.get())) {
            // Graphite/Beryllium moderator efficiency
            double moderated = data.fastNeutrons * 0.7;
            data.nextFastNeutrons -= moderated;
            data.nextThermalNeutrons += moderated * 0.95; // Some loss
            data.nextHeat += moderated * 0.01;
        } else if (state.is(Registration.CONTROL_CHANNEL.get()) && be instanceof com.unnamednuclear.block.ReactorChannelBlockEntity channel) {
            if (channel.getItem().is(Registration.CONTROL_ROD_ITEM.get())) {
                // Future: support fractional insertion via block state or item data
                data.nextFastNeutrons -= data.fastNeutrons * 0.5;
                data.nextThermalNeutrons -= data.thermalNeutrons * 0.99;
            }
        } else if (state.is(Registration.COOLANT_CHANNEL.get())) {
            // Active cooling will be handled by fluid exchange later, but basic heat removal for now
            data.nextHeat -= data.heat * 0.3;
        }
        
        // Radioactive Decay: I-135 -> Xe-135 -> Cs-135
        double iDecay = data.iodine135 * 0.05;
        data.nextIodine135 -= iDecay;
        data.nextXenon135 += iDecay;
        
        double xeDecay = data.xenon135 * 0.02;
        data.nextXenon135 -= xeDecay;
        
        // Ambient cooling
        data.nextHeat -= data.heat * 0.005;

        // Meltdown check
        if (data.heat > 2000) {
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 4.0f, Level.ExplosionInteraction.BLOCK);
            level.destroyBlock(pos, false);
        }
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
