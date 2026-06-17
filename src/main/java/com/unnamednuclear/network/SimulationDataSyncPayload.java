package com.unnamednuclear.network;

import com.unnamednuclear.UnnamedNuclear;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public record SimulationDataSyncPayload(Map<BlockPos, NodeData> nodes) implements CustomPacketPayload {
    public static final Type<SimulationDataSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "simulation_data"));

    public record NodeData(float fast, float thermal, float heat) {
        public static final StreamCodec<FriendlyByteBuf, NodeData> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, NodeData::fast,
                ByteBufCodecs.FLOAT, NodeData::thermal,
                ByteBufCodecs.FLOAT, NodeData::heat,
                NodeData::new
        );
    }

    public static final StreamCodec<FriendlyByteBuf, SimulationDataSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeInt(payload.nodes.size());
                for (Map.Entry<BlockPos, NodeData> entry : payload.nodes.entrySet()) {
                    buf.writeBlockPos(entry.getKey());
                    NodeData.STREAM_CODEC.encode(buf, entry.getValue());
                }
            },
            buf -> {
                int size = buf.readInt();
                Map<BlockPos, NodeData> nodes = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    nodes.put(buf.readBlockPos(), NodeData.STREAM_CODEC.decode(buf));
                }
                return new SimulationDataSyncPayload(nodes);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
