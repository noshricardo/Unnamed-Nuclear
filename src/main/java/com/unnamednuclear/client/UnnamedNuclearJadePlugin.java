package com.unnamednuclear.client;

import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.block.ReactorControllerBlockEntity;
import com.unnamednuclear.registration.Registration;
import com.unnamednuclear.simulation.SimulationNode;
import com.unnamednuclear.simulation.WorldSimulationData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class UnnamedNuclearJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(SimulationComponentProvider.INSTANCE, net.minecraft.world.level.block.entity.BlockEntity.class);
        registration.registerBlockDataProvider(ReactorComponentProvider.INSTANCE, ReactorControllerBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(SimulationComponentProvider.INSTANCE, net.minecraft.world.level.block.Block.class);
        registration.registerBlockComponent(ReactorComponentProvider.INSTANCE, net.minecraft.world.level.block.Block.class);
    }

    public static class ReactorComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        public static final ReactorComponentProvider INSTANCE = new ReactorComponentProvider();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getServerData().contains("reactor_data")) {
                net.minecraft.nbt.CompoundTag data = accessor.getServerData().getCompound("reactor_data");
                if (data.getBoolean("assembled")) {
                    tooltip.add(Component.literal("Status: Assembled").withStyle(net.minecraft.ChatFormatting.GREEN));
                    tooltip.add(Component.literal("Core Temperature: ").append(String.format("%.2f", data.getDouble("temp"))));
                    tooltip.add(Component.literal("Net Flux: ").append(String.format("%.2f", data.getDouble("flux"))));
                } else {
                    tooltip.add(Component.literal("Status: Incomplete").withStyle(net.minecraft.ChatFormatting.RED));
                }
            }
        }

        @Override
        public void appendServerData(net.minecraft.nbt.CompoundTag tag, BlockAccessor accessor) {
            if (accessor.getBlockEntity() instanceof ReactorControllerBlockEntity reactor) {
                net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
                data.putBoolean("assembled", reactor.isAssembled());
                data.putDouble("temp", reactor.getCoreTemperature());
                data.putDouble("flux", reactor.getNetFlux());
                tag.put("reactor_data", data);
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "reactor");
        }
    }

    public static class SimulationComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        public static final SimulationComponentProvider INSTANCE = new SimulationComponentProvider();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getServerData().contains("nuclear_data")) {
                net.minecraft.nbt.CompoundTag data = accessor.getServerData().getCompound("nuclear_data");
                tooltip.add(Component.literal("Heat: ").append(String.format("%.2f", data.getDouble("heat"))));
                tooltip.add(Component.literal("Fast Neutrons: ").append(String.format("%.2f", data.getDouble("fast"))));
                tooltip.add(Component.literal("Thermal Neutrons: ").append(String.format("%.2f", data.getDouble("thermal"))));
                if (data.contains("emissions")) {
                    tooltip.add(Component.literal("Neutron Emissions: ").append(String.format("%.2f n/t", data.getDouble("emissions"))).withStyle(net.minecraft.ChatFormatting.GOLD));
                }
                if (data.contains("xenon")) {
                    tooltip.add(Component.literal("Xenon-135: ").append(String.format("%.4f", data.getDouble("xenon"))));
                }
            }
        }

        @Override
        public void appendServerData(net.minecraft.nbt.CompoundTag tag, BlockAccessor accessor) {
            if (accessor.getLevel() instanceof ServerLevel serverLevel) {
                BlockPos pos = accessor.getPosition();
                SimulationNode node = WorldSimulationData.get(serverLevel).getNode(pos);
                if (node != null) {
                    net.minecraft.nbt.CompoundTag data = new net.minecraft.nbt.CompoundTag();
                    data.putDouble("heat", node.heat);
                    data.putDouble("fast", node.fastNeutrons);
                    data.putDouble("thermal", node.thermalNeutrons);
                    data.putDouble("emissions", node.emissions);
                    data.putDouble("xenon", node.composition.getAmount(ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "xe135")));
                    tag.put("nuclear_data", data);
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return ResourceLocation.fromNamespaceAndPath(UnnamedNuclear.MODID, "simulation");
        }
    }
}
