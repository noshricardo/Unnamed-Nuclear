package com.unnamednuclear.block;

import com.unnamednuclear.registration.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DebugNeutronSourceBlockEntity extends BlockEntity {
    public DebugNeutronSourceBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.DEBUG_NEUTRON_SOURCE_BE.get(), pos, state);
    }
}
