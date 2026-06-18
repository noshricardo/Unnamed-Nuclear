package com.unnamednuclear.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record NuclearComposition(double u235, double u238, double pu239, double sr90, double cs137, double waste, double u234, double u236, double pu240) {
    public static final NuclearComposition EMPTY = new NuclearComposition(0, 0, 0, 0, 0, 0, 0, 0, 0);

    public static final Codec<NuclearComposition> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.DOUBLE.fieldOf("u235").forGetter(NuclearComposition::u235),
            Codec.DOUBLE.fieldOf("u238").forGetter(NuclearComposition::u238),
            Codec.DOUBLE.fieldOf("pu239").forGetter(NuclearComposition::pu239),
            Codec.DOUBLE.fieldOf("sr90").forGetter(NuclearComposition::sr90),
            Codec.DOUBLE.fieldOf("cs137").forGetter(NuclearComposition::cs137),
            Codec.DOUBLE.fieldOf("waste").forGetter(NuclearComposition::waste),
            Codec.DOUBLE.fieldOf("u234").forGetter(NuclearComposition::u234),
            Codec.DOUBLE.fieldOf("u236").forGetter(NuclearComposition::u236),
            Codec.DOUBLE.fieldOf("pu240").forGetter(NuclearComposition::pu240)
        ).apply(instance, NuclearComposition::new)
    );
    
    public static final StreamCodec<ByteBuf, NuclearComposition> STREAM_CODEC = new StreamCodec<ByteBuf, NuclearComposition>() {
        @Override
        public NuclearComposition decode(ByteBuf buffer) {
            return new NuclearComposition(
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble(),
                buffer.readDouble(), buffer.readDouble(), buffer.readDouble()
            );
        }

        @Override
        public void encode(ByteBuf buffer, NuclearComposition value) {
            buffer.writeDouble(value.u235());
            buffer.writeDouble(value.u238());
            buffer.writeDouble(value.pu239());
            buffer.writeDouble(value.sr90());
            buffer.writeDouble(value.cs137());
            buffer.writeDouble(value.waste());
            buffer.writeDouble(value.u234());
            buffer.writeDouble(value.u236());
            buffer.writeDouble(value.pu240());
        }
    };

    public double getFissileContent() {
        return u235 + pu239;
    }

    public double getTotal() {
        return u235 + u238 + pu239 + sr90 + cs137 + waste + u234 + u236 + pu240;
    }

    public NuclearComposition normalize() {
        double total = getTotal();
        if (total <= 0) return this;
        return new NuclearComposition(u235 / total, u238 / total, pu239 / total, sr90 / total, cs137 / total, waste / total, u234 / total, u236 / total, pu240 / total);
    }
}
