package com.unnamednuclear.simulation;

import com.unnamednuclear.network.SimulationDataSyncPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.registration.Registration;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = UnnamedNuclear.MODID)
public class SimulationEventHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            if (serverLevel.getGameTime() % 20 == 0) {
                WorldSimulationData data = WorldSimulationData.get(serverLevel);
                data.tick(serverLevel);
                syncToDebugPlayers(serverLevel, data);
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = event.getPos();
            if (isReactorBlock(event.getState())) {
                WorldSimulationData.get(serverLevel).addNode(pos);
            }
            if (isReactorBlock(event.getState()) || event.getState().is(Registration.REACTOR_CASING.get())) {
                notifyController(serverLevel, pos);
            }
        }
    }

    private static void syncToDebugPlayers(ServerLevel level, WorldSimulationData data) {
        Map<BlockPos, SimulationDataSyncPayload.NodeData> syncData = null;
        
        for (ServerPlayer player : level.players()) {
            if (player.getMainHandItem().is(Registration.DEBUG_ITEM.get()) || player.getOffhandItem().is(Registration.DEBUG_ITEM.get())) {
                if (syncData == null) {
                    syncData = new HashMap<>();
                    for (BlockPos pos : data.getNodes().keySet()) {
                        SimulationNode node = data.getNode(pos);
                        syncData.put(pos, new SimulationDataSyncPayload.NodeData((float)node.fastNeutrons, (float)node.thermalNeutrons, (float)node.heat, (float)node.composition.getAmount(ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "xe135")), (float)node.emissions));
                    }
                }
                PacketDistributor.sendToPlayer(player, new SimulationDataSyncPayload(syncData));
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = event.getPos();
            if (isReactorBlock(event.getState())) {
                WorldSimulationData.get(serverLevel).removeNode(pos);
            }
            if (isReactorBlock(event.getState()) || event.getState().is(Registration.REACTOR_CASING.get())) {
                notifyController(serverLevel, pos);
            }
        }
    }

    private static void notifyController(ServerLevel level, BlockPos pos) {
        // Find nearby controllers and mark them for reassembly
        // Since multiblocks are relatively small, a simple search in a box is efficient enough
        // especially since this only happens on block place/break.
        int range = 16; 
        BlockPos.betweenClosedStream(pos.offset(-range, -range, -range), pos.offset(range, range, range))
            .forEach(p -> {
                if (level.getBlockEntity(p) instanceof com.unnamednuclear.block.ReactorControllerBlockEntity controller) {
                    controller.setNeedsReassembly();
                }
            });
    }

    private static boolean isReactorBlock(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Registration.FUEL_CHANNEL.get()) ||
               state.is(Registration.MODERATOR.get()) ||
               state.is(Registration.CONTROL_CHANNEL.get()) ||
               state.is(Registration.COOLANT_CHANNEL.get()) ||
               state.is(Registration.DEBUG_NEUTRON_SOURCE.get());
    }
}
