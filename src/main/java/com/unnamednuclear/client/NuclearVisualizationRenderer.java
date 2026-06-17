package com.unnamednuclear.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.unnamednuclear.network.SimulationDataSyncPayload;
import com.unnamednuclear.registration.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Map;

public class NuclearVisualizationRenderer {
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!(mc.player.getMainHandItem().is(Registration.DEBUG_ITEM.get()) || mc.player.getOffhandItem().is(Registration.DEBUG_ITEM.get()))) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();

        Map<BlockPos, SimulationDataSyncPayload.NodeData> nodes = ClientSimulationData.getNodes();

        for (Map.Entry<BlockPos, SimulationDataSyncPayload.NodeData> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            SimulationDataSyncPayload.NodeData data = entry.getValue();

            poseStack.pushPose();
            poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

            VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
            
            // Color based on activity
            float r = data.heat() > 0 ? Math.min(1.0f, data.heat() / 1000.0f) : 0.0f;
            float g = data.thermal() > 0 ? Math.min(1.0f, data.thermal() / 100.0f) : 0.0f;
            float b = data.fast() > 0 ? Math.min(1.0f, data.fast() / 100.0f) : 0.0f;
            
            if (r == 0 && g == 0 && b == 0) {
                // Minimum visibility for active nodes
                g = 0.2f;
            }

            LevelRenderer.renderLineBox(poseStack, buffer, 0.1, 0.1, 0.1, 0.9, 0.9, 0.9, r, g, b, 1.0f);

            poseStack.popPose();
        }
    }
}
