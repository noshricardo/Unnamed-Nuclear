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

    public double getModerationEfficiency(BlockState state, SimulationNode data) {
        if (state.is(Registration.MODERATOR.get())) {
            return 0.8; // Graphite
        }
        return 0;
    }

    protected double getModerationLoss(BlockState state) {
        return 0.02;
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
                    // Xenon-135 absorption cross-section is massive
                    double xenonEffect = data.xenon135 * 2000.0;
                    double absorptionProbability = xenonEffect / (xenonEffect + 1.0);
                    double absorbedByXenon = data.thermalNeutrons * 0.9 * absorptionProbability;
                    data.nextThermalNeutrons -= absorbedByXenon;

                    double availableThermal = Math.max(0, data.thermalNeutrons - absorbedByXenon);
                    
                    // Fission: Thermal neutrons cause fission in U-235 and Pu-239
                    double fissionYield = 0.8;
                    double fissionU235 = availableThermal * fissionYield * comp.u235();
                    double fissionPu239 = availableThermal * fissionYield * 1.2 * comp.pu239(); // Pu239 is more fissile
                    
                    // Spontaneous fission (source)
                    double sourceU235 = 0.0001 * comp.u235();
                    
                    double totalFissions = fissionU235 + fissionPu239 + sourceU235;
                    
                    data.nextFastNeutrons += totalFissions * getNeutronsPerFission();
                    data.nextHeat += totalFissions * getHeatPerFission();
                    data.nextThermalNeutrons -= (fissionU235 + fissionPu239);

                    // Iodine-135 production from fission
                    data.nextIodine135 += totalFissions * 0.0639; // Fission yield for I-135
                    
                    // Breeding: Fast neutrons captured by U-238 to eventually form Pu-239
                    double breedCapture = data.fastNeutrons * 0.15 * comp.u238();
                    data.nextFastNeutrons -= breedCapture;

                    // Fuel depletion logic (every 20 ticks to save performance)
                    double consumeRate = 0.001;
                    double newU235 = Math.max(0, comp.u235() - fissionU235 * consumeRate);
                    double newU238 = Math.max(0, comp.u238() - breedCapture * consumeRate);
                    double newPu239 = Math.max(0, comp.pu239() + (breedCapture - fissionPu239) * consumeRate);
                    double newWaste = comp.waste() + totalFissions * consumeRate;
                    
                    stack.set(Registration.COMPOSITION.get(), new com.unnamednuclear.item.NuclearComposition(
                        newU235, newU238, newPu239, comp.sr90(), comp.cs137(), newWaste, 
                        comp.u234(), comp.u236(), comp.pu240()));
                    channel.setChanged();
                }
            }
        } else {
            double moderationEfficiency = getModerationEfficiency(state, data);
            
            // Temperature/Void feedback
            double feedback = 1.0 + (data.heat * getTemperatureCoefficient());
            if (state.is(Registration.COOLANT_CHANNEL.get())) {
                feedback += getVoidCoefficient() * (data.heat / 2000.0); // Simple void model: hotter = more steam
            }
            moderationEfficiency *= Math.max(0.1, feedback);

            if (moderationEfficiency > 0) {
                double slowed = data.fastNeutrons * moderationEfficiency;
                data.nextFastNeutrons -= slowed;
                data.nextThermalNeutrons += slowed * (1.0 - getModerationLoss(state));
            }
            
            if (state.is(Registration.CONTROL_CHANNEL.get()) && be instanceof com.unnamednuclear.block.ReactorChannelBlockEntity channel) {
                double insertion = state.getValue(com.unnamednuclear.block.ReactorChannelBlock.INSERTION) / 10.0;
                if (channel.getItem().is(Registration.CONTROL_ROD_ITEM.get())) {
                    data.nextThermalNeutrons -= data.thermalNeutrons * 0.95 * insertion;
                    data.nextFastNeutrons -= data.fastNeutrons * 0.1 * insertion;
                }
            } else if (state.is(Registration.COOLANT_CHANNEL.get())) {
                data.nextHeat -= data.heat * 0.1;
            }
        }
        
        // Decay and ambient cooling
        simulateDecay(data);
        data.nextHeat -= data.heat * 0.001;

        if (data.heat > 5000) {
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 6.0f, Level.ExplosionInteraction.BLOCK);
            level.destroyBlock(pos, false);
        }
    }

    protected void simulateDecay(SimulationNode data) {
        double iodineDecayConst = 0.0001;
        double xenonDecayConst = 0.00007;
        
        double iDecay = data.iodine135 * iodineDecayConst;
        data.nextIodine135 -= iDecay;
        data.nextXenon135 += iDecay;
        
        double xeDecay = data.xenon135 * xenonDecayConst;
        data.nextXenon135 -= xeDecay;
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
