package com.unnamednuclear.simulation;

import com.unnamednuclear.registration.Registration;
import com.unnamednuclear.item.NuclearComposition;
import com.unnamednuclear.UnnamedNuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class SimulationEngine {

    public static void simulateNode(Level level, BlockPos pos, BlockState state, SimulationNode data) {
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (state.is(Registration.FUEL_CHANNEL.get()) && be instanceof com.unnamednuclear.block.ReactorChannelBlockEntity channel) {
            net.minecraft.world.item.ItemStack stack = channel.getItem();
            if (stack.is(Registration.NUCLEAR_FUEL.get())) {
                NuclearComposition comp = stack.get(Registration.COMPOSITION.get());
                if (comp == null) {
                    double enrichment = stack.getOrDefault(Registration.ENRICHMENT.get(), 0.02);
                    comp = new NuclearComposition(Map.of(
                            id("u235"), enrichment,
                            id("u238"), 1.0 - enrichment
                    ));
                    stack.set(Registration.COMPOSITION.get(), comp);
                }

                if (comp.getTotal() > 0) {
                    Map<ResourceLocation, Double> nextAmounts = new HashMap<>(comp.amounts());
                    double consumeRate = 0.001;

                    for (Map.Entry<ResourceLocation, Double> entry : comp.amounts().entrySet()) {
                        Isotope isotope = IsotopeRegistry.get(entry.getKey());
                        if (isotope == null) continue;

                        double amount = entry.getValue();
                        if (amount <= 0) continue;

                        if (isotope.reactions().isPresent()) {
                            for (Isotope.Reaction reaction : isotope.reactions().get()) {
                                double reactionRate = 0;
                                switch (reaction.type()) {
                                    case DECAY:
                                        // Decay is handled in simulateDecay for the node, but for fuel items we should also handle it.
                                        // For simplicity, let's keep decay in simulateDecay and handle neutron reactions here.
                                        break;
                                    case THERMAL_CAPTURE:
                                        reactionRate = data.thermalNeutrons * reaction.crossSection() * 0.001;
                                        break;
                                    case FAST_CAPTURE:
                                        reactionRate = data.fastNeutrons * reaction.crossSection() * 0.001;
                                        break;
                                    case THERMAL_FISSION:
                                        reactionRate = data.thermalNeutrons * reaction.crossSection() * 0.001;
                                        break;
                                    case FAST_FISSION:
                                        reactionRate = data.fastNeutrons * reaction.crossSection() * 0.001;
                                        break;
                                }

                                if (reactionRate > 0) {
                                    double actualReaction = Math.min(amount, reactionRate * amount * consumeRate);
                                    
                                    // Update node state
                                    double produced = (actualReaction / consumeRate) * reaction.neutronsProduced();
                                    data.nextFastNeutrons += produced;
                                    data.nextEmissions += produced;
                                    data.nextHeat += (actualReaction / consumeRate) * reaction.energyReleased() * 0.1;
                                    
                                    if (reaction.type() == Isotope.ReactionType.THERMAL_CAPTURE || reaction.type() == Isotope.ReactionType.THERMAL_FISSION) {
                                        data.nextThermalNeutrons -= actualReaction / consumeRate;
                                    } else if (reaction.type() == Isotope.ReactionType.FAST_CAPTURE || reaction.type() == Isotope.ReactionType.FAST_FISSION) {
                                        data.nextFastNeutrons -= actualReaction / consumeRate;
                                    }

                                    // Update composition
                                    nextAmounts.put(isotope.id(), nextAmounts.get(isotope.id()) - actualReaction);
                                    for (Map.Entry<ResourceLocation, Double> product : reaction.productYields().entrySet()) {
                                        nextAmounts.put(product.getKey(), nextAmounts.getOrDefault(product.getKey(), 0.0) + actualReaction * product.getValue());
                                    }
                                }
                            }
                        }
                    }

                    stack.set(Registration.COMPOSITION.get(), new NuclearComposition(nextAmounts));
                    channel.setChanged();
                }
            }
        } else if (state.is(Registration.DEBUG_NEUTRON_SOURCE.get())) {
            data.nextFastNeutrons += 100.0;
            data.nextEmissions += 100.0;
        } else if (state.is(Registration.DEBUG_NEUTRON_ABSORBER.get())) {
            data.nextFastNeutrons = 0;
            data.nextThermalNeutrons = 0;
        } else if (state.is(Registration.NEUTRON_ABSORBER.get())) {
            data.nextFastNeutrons -= data.fastNeutrons * 0.9;
            data.nextThermalNeutrons -= data.thermalNeutrons * 0.9;
        }

        // Apply environmental interaction for non-reactor blocks
        if (!isSimulationBlock(state)) {
            if (state.is(Registration.REACTOR_CASING.get()) || state.is(Registration.REACTOR_CONTROLLER.get())) {
                data.moderationEfficiency = 0.05;
                data.moderationLoss = 0.01;
                data.nextThermalNeutrons -= data.thermalNeutrons * 0.02;
            } else if (state.isAir()) {
                data.moderationEfficiency = 0.001;
                data.moderationLoss = 0.001;
                data.nextThermalNeutrons -= data.thermalNeutrons * 0.001;
            } else {
                // Generic solid block
                data.moderationEfficiency = 0.02;
                data.moderationLoss = 0.01;
                data.nextThermalNeutrons -= data.thermalNeutrons * 0.05;
                data.nextFastNeutrons -= data.fastNeutrons * 0.01;
            }
        }

        double moderationEfficiency = data.moderationEfficiency;

        // Temperature/Void feedback
        double feedback = 1.0 + (data.heat * data.temperatureCoefficient);
        if (state.is(Registration.COOLANT_CHANNEL.get())) {
            feedback += data.voidCoefficient * (data.heat / 2000.0);
        }
        moderationEfficiency *= Math.max(0.1, feedback);

        if (moderationEfficiency > 0) {
            double slowed = data.fastNeutrons * moderationEfficiency;
            data.nextFastNeutrons -= slowed;
            data.nextThermalNeutrons += slowed * (1.0 - data.moderationLoss);
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
        
        // Decay and ambient cooling
        simulateDecay(data);
        data.nextHeat -= data.heat * 0.001;

        if (data.heat > 5000) {
            level.explode(null, pos.getX(), pos.getY(), pos.getZ(), 6.0f, Level.ExplosionInteraction.BLOCK);
            level.destroyBlock(pos, false);
        }
    }

    private static void simulateDecay(SimulationNode data) {
        if (data.composition.amounts().isEmpty()) return;

        for (Map.Entry<ResourceLocation, Double> entry : data.composition.amounts().entrySet()) {
            Isotope isotope = IsotopeRegistry.get(entry.getKey());
            if (isotope != null && isotope.halfLifeTicks() > 0 && isotope.reactions().isPresent()) {
                double lambda = Math.log(2) / isotope.halfLifeTicks();
                double amount = entry.getValue();
                double decayedTotal = amount * lambda;

                for (Isotope.Reaction reaction : isotope.reactions().get()) {
                    if (reaction.type() == Isotope.ReactionType.DECAY) {
                        double actualDecay = decayedTotal * reaction.probability();
                        data.nextComposition.put(isotope.id(), data.nextComposition.getOrDefault(isotope.id(), 0.0) - actualDecay);
                        for (Map.Entry<ResourceLocation, Double> product : reaction.productYields().entrySet()) {
                            data.nextComposition.put(product.getKey(), data.nextComposition.getOrDefault(product.getKey(), 0.0) + actualDecay * product.getValue());
                        }
                    }
                }
            }
        }
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, path);
    }

    public static boolean isSimulationBlock(BlockState state) {
        return state.is(Registration.FUEL_CHANNEL.get()) ||
               state.is(Registration.MODERATOR.get()) ||
               state.is(Registration.CONTROL_CHANNEL.get()) ||
               state.is(Registration.COOLANT_CHANNEL.get()) ||
               state.is(Registration.DEBUG_NEUTRON_SOURCE.get()) ||
               state.is(Registration.NEUTRON_REFLECTOR.get()) ||
               state.is(Registration.NEUTRON_ABSORBER.get()) ||
               state.is(Registration.DEBUG_NEUTRON_REFLECTOR.get()) ||
               state.is(Registration.DEBUG_NEUTRON_ABSORBER.get());
    }

    public static void diffuse(Level level, BlockPos pos, Direction dir, SimulationNode data, SimulationNode neighborData) {
        BlockPos neighborPos = pos.relative(dir);
        net.minecraft.world.level.block.state.BlockState neighborState = level.getBlockState(neighborPos);

        double diffusedFast = data.fastNeutrons * 0.1;
        double diffusedThermal = data.thermalNeutrons * 0.1;
        double diffusedHeat = data.heat * 0.05;

        // Reflection logic
        if (neighborState.is(Registration.DEBUG_NEUTRON_REFLECTOR.get())) {
            // 100% reflection: don't remove neutrons from data.next...
            // Or remove them and add them back. Let's just not remove them.
            diffusedFast = 0;
            diffusedThermal = 0;
        } else if (neighborState.is(Registration.NEUTRON_REFLECTOR.get())) {
            // 90% reflection
            diffusedFast *= 0.1;
            diffusedThermal *= 0.1;
        }

        data.nextFastNeutrons -= diffusedFast;
        data.nextThermalNeutrons -= diffusedThermal;
        data.nextHeat -= diffusedHeat;

        if (neighborData != null) {
            neighborData.nextFastNeutrons += diffusedFast;
            neighborData.nextThermalNeutrons += diffusedThermal;
            neighborData.nextHeat += diffusedHeat;
        }

        if (!data.composition.amounts().isEmpty()) {
            for (Map.Entry<ResourceLocation, Double> entry : data.composition.amounts().entrySet()) {
                double diffused = entry.getValue() * 0.05;
                data.nextComposition.put(entry.getKey(), data.nextComposition.getOrDefault(entry.getKey(), 0.0) - diffused);
                if (neighborData != null) {
                    neighborData.nextComposition.put(entry.getKey(), neighborData.nextComposition.getOrDefault(entry.getKey(), 0.0) + diffused);
                }
            }
        }
    }
}
