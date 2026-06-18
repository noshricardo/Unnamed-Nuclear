package com.unnamednuclear.simulation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WorldSimulationData extends SavedData {
    private final Map<BlockPos, SimulationNode> nodes = new HashMap<>();
    private final Map<BlockPos, ReactorType> types = new HashMap<>();

    public static WorldSimulationData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(WorldSimulationData::new, WorldSimulationData::load), "unnamednuclear_simulation");
    }

    public SimulationNode getNode(BlockPos pos) {
        return nodes.get(pos);
    }

    public Map<BlockPos, SimulationNode> getNodes() {
        return nodes;
    }

    public void addNode(BlockPos pos, ReactorType type) {
        nodes.putIfAbsent(pos, new SimulationNode());
        types.put(pos, type);
    }

    public void removeNode(BlockPos pos) {
        nodes.remove(pos);
        types.remove(pos);
    }

    public void tick(ServerLevel level) {
        // 1. Generation
        for (Map.Entry<BlockPos, SimulationNode> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            SimulationNode node = entry.getValue();
            ReactorType type = types.get(pos);
            if (type != null) {
                type.simulateNode(level, pos, level.getBlockState(pos), node);
            }
        }

        // 2. Diffusion
        for (Map.Entry<BlockPos, SimulationNode> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            SimulationNode node = entry.getValue();
            ReactorType type = types.get(pos);
            if (type != null) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighborPos = pos.relative(dir);
                    SimulationNode neighborNode = nodes.get(neighborPos);
                    // Even if neighborNode is null, we might want to diffuse (loss)
                    type.diffuse(level, pos, node, neighborNode);
                }
            }
        }

        // 3. Update
        for (SimulationNode node : nodes.values()) {
            node.update();
        }
        
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, SimulationNode> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            SimulationNode node = entry.getValue();
            CompoundTag nodeTag = new CompoundTag();
            nodeTag.putLong("pos", pos.asLong());
            nodeTag.putDouble("fast", node.fastNeutrons);
            nodeTag.putDouble("thermal", node.thermalNeutrons);
            nodeTag.putDouble("heat", node.heat);
            nodeTag.putDouble("iodine", node.iodine135);
            nodeTag.putDouble("xenon", node.xenon135);
            nodeTag.putString("type", types.get(pos).getId());
            list.add(nodeTag);
        }
        tag.put("nodes", list);
        return tag;
    }

    public static WorldSimulationData load(CompoundTag tag, HolderLookup.Provider registries) {
        WorldSimulationData data = new WorldSimulationData();
        ListTag list = tag.getList("nodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag nodeTag = list.getCompound(i);
            BlockPos pos = BlockPos.of(nodeTag.getLong("pos"));
            SimulationNode node = new SimulationNode();
            node.fastNeutrons = nodeTag.getDouble("fast");
            node.thermalNeutrons = nodeTag.getDouble("thermal");
            node.heat = nodeTag.getDouble("heat");
            node.iodine135 = nodeTag.getDouble("iodine");
            node.xenon135 = nodeTag.getDouble("xenon");
            data.nodes.put(pos, node);
            
            String typeId = nodeTag.getString("type");
            ReactorType type = switch (typeId) {
                case "rbmk" -> RBMKReactorType.INSTANCE;
                case "sodium_fast" -> SodiumFastReactorType.INSTANCE;
                case "pwr" -> PWRReactorType.INSTANCE;
                default -> DefaultReactorType.INSTANCE;
            };
            data.types.put(pos, type);
        }
        return data;
    }
}
