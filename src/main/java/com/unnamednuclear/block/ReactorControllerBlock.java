package com.unnamednuclear.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ReactorControllerBlock extends Block implements EntityBlock {
    public ReactorControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ReactorControllerBlockEntity controller) {
                player.sendSystemMessage(Component.literal("Reactor Status:"));
                player.sendSystemMessage(Component.literal(" - Assembled: " + controller.isAssembled()));
                if (controller.isAssembled()) {
                    player.sendSystemMessage(Component.literal(" - Total Heat: " + String.format("%.2f", controller.getTotalHeat())));
                    player.sendSystemMessage(Component.literal(" - Interior Size: " + controller.getInteriorSize()));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorControllerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : (l, p, s, be) -> {
            if (be instanceof ReactorControllerBlockEntity controller) {
                controller.tick();
            }
        };
    }
}
