package com.unnamednuclear.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ReactorChannelBlock extends Block implements EntityBlock {
    public static final IntegerProperty INSERTION = IntegerProperty.create("insertion", 0, 10);

    public ReactorChannelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(INSERTION, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INSERTION);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorChannelBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        if (player.isSecondaryUseActive()) {
            int current = state.getValue(INSERTION);
            int next = (current + 1) % 11;
            level.setBlock(pos, state.setValue(INSERTION, next), 3);
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ReactorChannelBlockEntity channel) {
            ItemStack stackInHand = player.getItemInHand(player.getUsedItemHand());
            ItemStack stackInChannel = channel.getItem();

            if (stackInHand.isEmpty() && !stackInChannel.isEmpty()) {
                // Take item out
                player.setItemInHand(player.getUsedItemHand(), stackInChannel.copy());
                channel.setItem(ItemStack.EMPTY);
                return InteractionResult.SUCCESS;
            } else if (!stackInHand.isEmpty() && stackInChannel.isEmpty()) {
                // Put item in (take 1)
                ItemStack toInsert = stackInHand.copy();
                toInsert.setCount(1);
                channel.setItem(toInsert);
                stackInHand.shrink(1);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
