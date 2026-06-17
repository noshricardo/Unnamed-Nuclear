package com.unnamednuclear.client;

import com.unnamednuclear.network.SimulationDataSyncPayload;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

public class ClientSimulationData {
    private static Map<BlockPos, SimulationDataSyncPayload.NodeData> nodes = new HashMap<>();

    public static void update(Map<BlockPos, SimulationDataSyncPayload.NodeData> newNodes) {
        nodes = newNodes;
    }

    public static Map<BlockPos, SimulationDataSyncPayload.NodeData> getNodes() {
        return nodes;
    }
}
