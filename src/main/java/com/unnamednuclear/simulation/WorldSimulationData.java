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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class WorldSimulationData extends SavedData {
    private final Map<BlockPos, SimulationNode> nodes = new HashMap<>();

    public static WorldSimulationData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(WorldSimulationData::new, WorldSimulationData::load), "unnamednuclear_simulation");
    }

    public SimulationNode getNode(BlockPos pos) {
        return nodes.get(pos);
    }

    public Map<BlockPos, SimulationNode> getNodes() {
        return nodes;
    }

    public void addNode(BlockPos pos) {
        nodes.putIfAbsent(pos, new SimulationNode());
    }

    public void removeNode(BlockPos pos) {
        nodes.remove(pos);
    }

    public void tick(ServerLevel level) {
        // 1. Generation
        for (Map.Entry<BlockPos, SimulationNode> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            SimulationNode node = entry.getValue();
            SimulationEngine.simulateNode(level, pos, level.getBlockState(pos), node);
        }

        // 2. Diffusion
        Map<BlockPos, SimulationNode> newNodes = new HashMap<>();
        for (Map.Entry<BlockPos, SimulationNode> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            SimulationNode node = entry.getValue();
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = pos.relative(dir);
                SimulationNode neighborNode = nodes.get(neighborPos);
                
                if (neighborNode == null) {
                    neighborNode = newNodes.get(neighborPos);
                }
                
                if (neighborNode == null) {
                    // Create new node if flux is significant
                    if (node.fastNeutrons > 0.01 || node.thermalNeutrons > 0.01 || node.heat > 0.1) {
                        neighborNode = new SimulationNode();
                        newNodes.put(neighborPos, neighborNode);
                    }
                }
                
                SimulationEngine.diffuse(level, pos, dir, node, neighborNode);
            }
        }
        nodes.putAll(newNodes);

        // 3. Update & Cleanup
        Iterator<Map.Entry<BlockPos, SimulationNode>> it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, SimulationNode> entry = it.next();
            SimulationNode node = entry.getValue();
            node.update();
            
            // Cleanup: remove non-simulation nodes that have no activity
            if (node.fastNeutrons < 0.001 && node.thermalNeutrons < 0.001 && node.heat < 0.01 && node.composition.amounts().isEmpty()) {
                if (!SimulationEngine.isSimulationBlock(level.getBlockState(entry.getKey()))) {
                    it.remove();
                }
            }
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
            
            CompoundTag compTag = new CompoundTag();
            for (Map.Entry<net.minecraft.resources.ResourceLocation, Double> e : node.composition.amounts().entrySet()) {
                compTag.putDouble(e.getKey().toString(), e.getValue());
            }
            nodeTag.put("composition", compTag);

            nodeTag.putDouble("fissionYield", node.fissionYield);
            nodeTag.putDouble("neutronsPerFission", node.neutronsPerFission);
            nodeTag.putDouble("heatPerFission", node.heatPerFission);
            nodeTag.putDouble("moderationEfficiency", node.moderationEfficiency);
            nodeTag.putDouble("moderationLoss", node.moderationLoss);
            nodeTag.putDouble("voidCoefficient", node.voidCoefficient);
            nodeTag.putDouble("temperatureCoefficient", node.temperatureCoefficient);

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
            
            if (nodeTag.contains("composition")) {
                CompoundTag compTag = nodeTag.getCompound("composition");
                Map<net.minecraft.resources.ResourceLocation, Double> amounts = new HashMap<>();
                for (String key : compTag.getAllKeys()) {
                    amounts.put(net.minecraft.resources.ResourceLocation.parse(key), compTag.getDouble(key));
                }
                node.composition = new com.unnamednuclear.item.NuclearComposition(amounts);
            }

            if (nodeTag.contains("fissionYield")) node.fissionYield = nodeTag.getDouble("fissionYield");
            if (nodeTag.contains("neutronsPerFission")) node.neutronsPerFission = nodeTag.getDouble("neutronsPerFission");
            if (nodeTag.contains("heatPerFission")) node.heatPerFission = nodeTag.getDouble("heatPerFission");
            if (nodeTag.contains("moderationEfficiency")) node.moderationEfficiency = nodeTag.getDouble("moderationEfficiency");
            if (nodeTag.contains("moderationLoss")) node.moderationLoss = nodeTag.getDouble("moderationLoss");
            if (nodeTag.contains("voidCoefficient")) node.voidCoefficient = nodeTag.getDouble("voidCoefficient");
            if (nodeTag.contains("temperatureCoefficient")) node.temperatureCoefficient = nodeTag.getDouble("temperatureCoefficient");

            data.nodes.put(pos, node);
        }
        return data;
    }
}
