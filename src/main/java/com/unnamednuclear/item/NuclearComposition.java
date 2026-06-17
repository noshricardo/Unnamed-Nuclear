package com.unnamednuclear.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NuclearComposition(double u235, double u238, double pu239, double waste) {
    public static final NuclearComposition EMPTY = new NuclearComposition(0, 0, 0, 0);

    public static final Codec<NuclearComposition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.DOUBLE.fieldOf("u235").forGetter(NuclearComposition::u235),
            Codec.DOUBLE.fieldOf("u238").forGetter(NuclearComposition::u238),
            Codec.DOUBLE.fieldOf("pu239").forGetter(NuclearComposition::pu239),
            Codec.DOUBLE.fieldOf("waste").forGetter(NuclearComposition::waste)
        ).apply(instance, NuclearComposition::new)
    );
    
    public static final StreamCodec<ByteBuf, NuclearComposition> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE, NuclearComposition::u235,
        ByteBufCodecs.DOUBLE, NuclearComposition::u238,
        ByteBufCodecs.DOUBLE, NuclearComposition::pu239,
        ByteBufCodecs.DOUBLE, NuclearComposition::waste,
        NuclearComposition::new
    );

    public double getFissileContent() {
        return u235 + pu239;
    }

    public double getTotal() {
        return u235 + u238 + pu239 + waste;
    }
}
