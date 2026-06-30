package com.unnamednuclear.simulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record Isotope(
        ResourceLocation id,
        double atomicMass,
        double halfLifeTicks, // ticks, <= 0 means stable
        Optional<List<Reaction>> reactions
) {
    public static final Codec<Isotope> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(Isotope::id),
                    Codec.DOUBLE.fieldOf("atomic_mass").forGetter(Isotope::atomicMass),
                    Codec.DOUBLE.fieldOf("half_life").forGetter(Isotope::halfLifeTicks),
                    Reaction.CODEC.listOf().optionalFieldOf("reactions").forGetter(Isotope::reactions)
            ).apply(instance, Isotope::new)
    );

    public record Reaction(
            ReactionType type,
            double crossSection, // for neutron capture/fission (barns-like scale)
            double probability, // for branching decays
            java.util.Map<ResourceLocation, Double> productYields, // resultIsotope -> yield
            double neutronsProduced,
            double energyReleased
    ) {
        public static final Codec<Reaction> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        ReactionType.CODEC.fieldOf("type").forGetter(Reaction::type),
                        Codec.DOUBLE.fieldOf("cross_section").orElse(0.0).forGetter(Reaction::crossSection),
                        Codec.DOUBLE.fieldOf("probability").orElse(1.0).forGetter(Reaction::probability),
                        Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE).fieldOf("products").forGetter(Reaction::productYields),
                        Codec.DOUBLE.fieldOf("neutrons").orElse(0.0).forGetter(Reaction::neutronsProduced),
                        Codec.DOUBLE.fieldOf("energy").orElse(0.0).forGetter(Reaction::energyReleased)
                ).apply(instance, Reaction::new)
        );
    }

    public enum ReactionType {
        DECAY,
        THERMAL_CAPTURE,
        FAST_CAPTURE,
        THERMAL_FISSION,
        FAST_FISSION;

        public static final Codec<ReactionType> CODEC = Codec.STRING.xmap(
                s -> ReactionType.valueOf(s.toUpperCase()),
                e -> e.name().toLowerCase()
        );
    }
}
