package com.unnamednuclear.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.registration.Registration;

@EventBusSubscriber(modid = UnnamedNuclear.MODID)
public class SimulationEventHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            WorldSimulationData.get(serverLevel).tick(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            BlockPos pos = event.getPos();
            if (isReactorBlock(event.getState())) {
                WorldSimulationData.get(serverLevel).addNode(pos, DefaultReactorType.INSTANCE);
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
        }
    }

    private static boolean isReactorBlock(net.minecraft.world.level.block.state.BlockState state) {
        return state.is(Registration.FUEL_CHANNEL.get()) ||
               state.is(Registration.MODERATOR.get()) ||
               state.is(Registration.CONTROL_CHANNEL.get()) ||
               state.is(Registration.COOLANT_CHANNEL.get());
    }
}
