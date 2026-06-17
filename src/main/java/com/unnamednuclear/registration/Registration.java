package com.unnamednuclear.registration;

import com.unnamednuclear.UnnamedNuclear;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.unnamednuclear.block.ReactorControllerBlock;
import com.unnamednuclear.block.ReactorControllerBlockEntity;

import com.unnamednuclear.item.DescBlockItem;

import java.util.function.Supplier;

public class Registration {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(UnnamedNuclear.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UnnamedNuclear.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, UnnamedNuclear.MODID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, UnnamedNuclear.MODID);

    public static final DeferredBlock<ReactorControllerBlock> REACTOR_CONTROLLER = register("reactor_controller", () -> new ReactorControllerBlock(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> REACTOR_CASING = register("reactor_casing", () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> FUEL_ROD = register("fuel_rod", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> MODERATOR = register("moderator", () -> new Block(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.STONE)));
    public static final DeferredBlock<Block> CONTROL_ROD = register("control_rod", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorControllerBlockEntity>> REACTOR_CONTROLLER_BE = BLOCK_ENTITIES.register("reactor_controller", () -> BlockEntityType.Builder.of(ReactorControllerBlockEntity::new, REACTOR_CONTROLLER.get()).build(null));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.unnamednuclear.block.ReactorMenu>> REACTOR_MENU = MENU_TYPES.register("reactor_controller", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.unnamednuclear.block.ReactorMenu::new));

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new DescBlockItem(deferredBlock.get(), new Item.Properties()));
        return deferredBlock;
    }
}
