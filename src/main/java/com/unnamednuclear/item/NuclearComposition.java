package com.unnamednuclear.item;

import com.mojang.serialization.Codec;
import com.unnamednuclear.UnnamedNuclear;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record NuclearComposition(Map<ResourceLocation, Double> amounts) {
    public static final NuclearComposition EMPTY = new NuclearComposition(Map.of());

    public static final Codec<NuclearComposition> CODEC = Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)
            .xmap(NuclearComposition::new, NuclearComposition::amounts);

    public static final StreamCodec<ByteBuf, NuclearComposition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.DOUBLE), NuclearComposition::amounts,
            NuclearComposition::new
    );

    public double getAmount(ResourceLocation id) {
        return amounts.getOrDefault(id, 0.0);
    }

    public double getTotal() {
        return amounts.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    // IDs for common isotopes (internal use only)
    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, path);
    }

    public double u235() { return getAmount(id("u235")); }
    public double u238() { return getAmount(id("u238")); }

    public NuclearComposition with(ResourceLocation id, double amount) {
        Map<ResourceLocation, Double> newMap = new HashMap<>(amounts);
        if (amount <= 0) {
            newMap.remove(id);
        } else {
            newMap.put(id, amount);
        }
        return new NuclearComposition(newMap);
    }

    public NuclearComposition normalize() {
        double total = getTotal();
        if (total <= 0) return EMPTY;
        Map<ResourceLocation, Double> newMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, Double> entry : amounts.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue() / total);
        }
        return new NuclearComposition(newMap);
    }
}
