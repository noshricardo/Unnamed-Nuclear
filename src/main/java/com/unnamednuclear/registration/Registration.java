package com.unnamednuclear.registration;

import com.mojang.serialization.Codec;
import com.unnamednuclear.UnnamedNuclear;
import com.unnamednuclear.block.ReactorChannelBlock;
import com.unnamednuclear.block.ReactorChannelBlockEntity;
import com.unnamednuclear.item.EnrichedItem;
import com.unnamednuclear.item.NuclearFuelItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
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
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, UnnamedNuclear.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, UnnamedNuclear.MODID);
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, UnnamedNuclear.MODID);

    public static final DeferredBlock<ReactorControllerBlock> REACTOR_CONTROLLER = register("reactor_controller", () -> new ReactorControllerBlock(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> REACTOR_CASING = register("reactor_casing", () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<ReactorChannelBlock> FUEL_CHANNEL = register("fuel_channel", () -> new ReactorChannelBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> MODERATOR = register("moderator", () -> new Block(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.STONE)));
    public static final DeferredBlock<ReactorChannelBlock> CONTROL_CHANNEL = register("control_channel", () -> new ReactorChannelBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<ReactorChannelBlock> COOLANT_CHANNEL = register("coolant_channel", () -> new ReactorChannelBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> URANIUM_ORE = register("uranium_ore", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore", () -> new Block(BlockBehaviour.Properties.of().strength(4.5f).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> FLUORITE_ORE = register("fluorite_ore", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CENTRIFUGE = register("centrifuge", () -> new com.unnamednuclear.block.CentrifugeBlock(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL)));

    public static final DeferredItem<Item> NUCLEAR_FUEL = ITEMS.register("nuclear_fuel", () -> new NuclearFuelItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CONTROL_ROD_ITEM = ITEMS.register("control_rod", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.register("raw_uranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> YELLOWCAKE = ITEMS.register("yellowcake", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> URANIUM_HEXAFLUORIDE = ITEMS.register("uranium_hexafluoride", () -> new EnrichedItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> FLUORITE = ITEMS.register("fluorite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENRICHED_URANIUM = ITEMS.register("enriched_uranium", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> DEPLETED_URANIUM = ITEMS.register("depleted_uranium", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> PLUTONIUM = ITEMS.register("plutonium", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> FISSION_PRODUCTS = ITEMS.register("fission_products", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GUIDEBOOK = ITEMS.register("guidebook", () -> new com.unnamednuclear.item.GuidebookItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<DataComponentType<String>> FUEL_TYPE = DATA_COMPONENTS.register("fuel_type", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final Supplier<DataComponentType<Double>> ENRICHMENT = DATA_COMPONENTS.register("enrichment", () -> DataComponentType.<Double>builder().persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE).build());
    public static final Supplier<DataComponentType<com.unnamednuclear.item.NuclearComposition>> COMPOSITION = DATA_COMPONENTS.register("composition", () -> DataComponentType.<com.unnamednuclear.item.NuclearComposition>builder().persistent(com.unnamednuclear.item.NuclearComposition.CODEC).networkSynchronized(com.unnamednuclear.item.NuclearComposition.STREAM_CODEC).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorControllerBlockEntity>> REACTOR_CONTROLLER_BE = BLOCK_ENTITIES.register("reactor_controller", () -> BlockEntityType.Builder.of(ReactorControllerBlockEntity::new, REACTOR_CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorChannelBlockEntity>> REACTOR_CHANNEL_BE = BLOCK_ENTITIES.register("reactor_channel", () -> BlockEntityType.Builder.of(ReactorChannelBlockEntity::new, FUEL_CHANNEL.get(), CONTROL_CHANNEL.get(), COOLANT_CHANNEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.unnamednuclear.block.CentrifugeBlockEntity>> CENTRIFUGE_BE = BLOCK_ENTITIES.register("centrifuge", () -> BlockEntityType.Builder.of(com.unnamednuclear.block.CentrifugeBlockEntity::new, CENTRIFUGE.get()).build(null));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.unnamednuclear.block.ReactorMenu>> REACTOR_MENU = MENU_TYPES.register("reactor_controller", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.unnamednuclear.block.ReactorMenu::new));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.unnamednuclear.block.CentrifugeMenu>> CENTRIFUGE_MENU = MENU_TYPES.register("centrifuge", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.unnamednuclear.block.CentrifugeMenu::new));

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new DescBlockItem(deferredBlock.get(), new Item.Properties()));
        return deferredBlock;
    }
}
