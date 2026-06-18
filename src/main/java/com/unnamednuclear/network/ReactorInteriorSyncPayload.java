package com.unnamednuclear.network;

import com.unnamednuclear.UnnamedNuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record ReactorInteriorSyncPayload(List<ChannelData> channels) implements CustomPacketPayload {
    public static final Type<ReactorInteriorSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "reactor_interior"));

    public record ChannelData(BlockPos pos, String type, ItemStack item, int insertion) {
        public static final StreamCodec<RegistryFriendlyByteBuf, ChannelData> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC.cast(), ChannelData::pos,
                ByteBufCodecs.STRING_UTF8.cast(), ChannelData::type,
                ItemStack.OPTIONAL_STREAM_CODEC, ChannelData::item,
                ByteBufCodecs.VAR_INT.cast(), ChannelData::insertion,
                ChannelData::new
        );
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ReactorInteriorSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.channels.size());
                for (ChannelData data : payload.channels) {
                    ChannelData.STREAM_CODEC.encode(buf, data);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<ChannelData> channels = new ArrayList<>(size);
                for (int i = 0; i < size; i++) {
                    channels.add(ChannelData.STREAM_CODEC.decode(buf));
                }
                return new ReactorInteriorSyncPayload(channels);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
