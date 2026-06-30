package com.unnamednuclear.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.unnamednuclear.network.SimulationDataSyncPayload;
import com.unnamednuclear.registration.Registration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

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
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (Map.Entry<BlockPos, SimulationDataSyncPayload.NodeData> entry : nodes.entrySet()) {
            BlockPos pos = entry.getKey();
            SimulationDataSyncPayload.NodeData data = entry.getValue();

            poseStack.pushPose();
            poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

            // Color based on activity
            float r = data.heat() > 0 ? Math.min(1.0f, data.heat() / 1000.0f) : 0.0f;
            float g = data.thermal() > 0 ? Math.min(1.0f, data.thermal() / 100.0f) : 0.0f;
            float b = data.fast() > 0 ? Math.min(1.0f, data.fast() / 100.0f) : 0.0f;

            if (r == 0 && g == 0 && b == 0) {
                // Minimum visibility for active nodes
                g = 0.2f;
            }

            LevelRenderer.renderLineBox(poseStack, bufferSource.getBuffer(RenderType.lines()), 0.1, 0.1, 0.1, 0.9, 0.9, 0.9, r, g, b, 1.0f);

            // Render field lines/raycasts
            for (Direction dir : Direction.values()) {
                float fluxFast = (float) data.fast() * 0.1f;
                float fluxThermal = (float) data.thermal() * 0.1f;

                if (fluxFast > 0.01f || fluxThermal > 0.01f) {
                    float lr = 0.0f;
                    float lg = (float) Math.min(1.0, Math.pow(fluxThermal / 5.0, 0.5));
                    float lb = (float) Math.min(1.0, Math.pow(fluxFast / 5.0, 0.5));

                    BlockPos neighborPos = pos.relative(dir);
                    float length = nodes.containsKey(neighborPos) ? 1.0f : 0.7f;

                    renderLine(poseStack, bufferSource, 0.5f, 0.5f, 0.5f,
                            0.5f + dir.getStepX() * length,
                            0.5f + dir.getStepY() * length,
                            0.5f + dir.getStepZ() * length,
                            lr, lg, lb, 0.8f);
                }
            }

            if (data.emissions() > 0) {
                String text = String.format("%.1f n/t", data.emissions());
                poseStack.pushPose();
                poseStack.translate(0.5, 1.1, 0.5);
                float scale = 0.02f;
                poseStack.scale(-scale, -scale, scale);
                poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
                mc.font.drawInBatch(text, -mc.font.width(text) / 2.0f, 0, 0xFFFFFFFF, false, poseStack.last().pose(), bufferSource, net.minecraft.client.gui.Font.DisplayMode.NORMAL, 0, 15728880);
                poseStack.popPose();

                // Visual raycasts for emissions
                long time = mc.level.getGameTime();
                for (int i = 0; i < 6; i++) {
                    float angle1 = (float) ((i * 60 + time * 5) * Math.PI / 180.0);
                    float dx = (float) Math.cos(angle1);
                    float dy = (float) Math.sin(angle1 * 0.5);
                    float dz = (float) Math.sin(angle1);

                    float intensity = Math.min(1.0f, data.emissions() / 100.0f);
                    renderLine(poseStack, bufferSource, 0.5f, 0.5f, 0.5f,
                            0.5f + dx * 0.7f, 0.5f + dy * 0.7f, 0.5f + dz * 0.7f,
                            1.0f, 1.0f, 1.0f, intensity * 0.4f);
                }
            }

            poseStack.popPose();
        }
    }

    private static void renderLine(PoseStack poseStack, MultiBufferSource bufferSource, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix4f = poseStack.last().pose();
        buffer.addVertex(matrix4f, x1, y1, z1).setColor(r, g, b, a).setNormal(poseStack.last(), 0, 1, 0);
        buffer.addVertex(matrix4f, x2, y2, z2).setColor(r, g, b, a).setNormal(poseStack.last(), 0, 1, 0);
    }
}
