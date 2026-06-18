package com.unnamednuclear.client;

import com.unnamednuclear.network.ReactorInteriorSyncPayload;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class ClientReactorData {
    private static List<ReactorInteriorSyncPayload.ChannelData> channels = new ArrayList<>();

    public static void update(List<ReactorInteriorSyncPayload.ChannelData> newChannels) {
        channels = newChannels;
    }

    public static List<ReactorInteriorSyncPayload.ChannelData> getChannels() {
        return channels;
    }
}
