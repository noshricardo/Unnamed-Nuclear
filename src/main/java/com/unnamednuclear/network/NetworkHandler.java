package com.unnamednuclear.network;

import com.unnamednuclear.client.ClientSimulationData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class NetworkHandler {
    public static void handleSimulationData(final SimulationDataSyncPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientSimulationData.update(payload.nodes());
        });
    }
}
