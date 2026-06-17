package com.unnamednuclear.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.unnamednuclear.block.ReactorControllerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class ReactorOverlayRenderer {
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPos = event.getCamera().getPosition();

        for (ReactorControllerBlockEntity reactor : ClientReactorTracker.getReactors()) {
            if (!reactor.isAssembled()) {
                for (BlockPos errorPos : reactor.getErrorPositions()) {
                    if (errorPos != null && !errorPos.equals(BlockPos.ZERO)) {
                        poseStack.pushPose();
                        poseStack.translate(errorPos.getX() - cameraPos.x, errorPos.getY() - cameraPos.y, errorPos.getZ() - cameraPos.z);
                        
                        VertexConsumer buffer = mc.renderBuffers().bufferSource().getBuffer(RenderType.lines());
                        LevelRenderer.renderLineBox(poseStack, buffer, 0, 0, 0, 1, 1, 1, 1.0f, 0.0f, 0.0f, 1.0f);
                        
                        poseStack.popPose();
                    }
                }
            }
        }
    }
}
