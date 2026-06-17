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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
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
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, UnnamedNuclear.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, UnnamedNuclear.MODID);

    public static final DeferredHolder<FluidType, FluidType> HF_TYPE = FLUID_TYPES.register("hydrofluoric_acid", () -> new FluidType(FluidType.Properties.create().density(1150).viscosity(1000)));
    public static final DeferredHolder<FluidType, FluidType> F2_TYPE = FLUID_TYPES.register("fluorine_gas", () -> new FluidType(FluidType.Properties.create().density(-1000).viscosity(500)));
    public static final DeferredHolder<FluidType, FluidType> UF6_TYPE = FLUID_TYPES.register("uranium_hexafluoride", () -> new FluidType(FluidType.Properties.create().density(4670).viscosity(1500)));
    public static final DeferredHolder<FluidType, FluidType> HNO3_TYPE = FLUID_TYPES.register("nitric_acid", () -> new FluidType(FluidType.Properties.create().density(1510).viscosity(1100)));
    public static final DeferredHolder<FluidType, FluidType> TBP_TYPE = FLUID_TYPES.register("tbp_kerosene", () -> new FluidType(FluidType.Properties.create().density(800).viscosity(2000)));

    public static final DeferredHolder<Fluid, Fluid> HF = FLUIDS.register("hydrofluoric_acid", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Source(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(HF_TYPE, Registration.HF, Registration.HF_FLOWING)));
    public static final DeferredHolder<Fluid, Fluid> HF_FLOWING = FLUIDS.register("hydrofluoric_acid_flowing", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(HF_TYPE, Registration.HF, Registration.HF_FLOWING)));

    public static final DeferredHolder<Fluid, Fluid> F2 = FLUIDS.register("fluorine_gas", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Source(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(F2_TYPE, Registration.F2, Registration.F2_FLOWING)));
    public static final DeferredHolder<Fluid, Fluid> F2_FLOWING = FLUIDS.register("fluorine_gas_flowing", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(F2_TYPE, Registration.F2, Registration.F2_FLOWING)));

    public static final DeferredHolder<Fluid, Fluid> UF6 = FLUIDS.register("uranium_hexafluoride", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Source(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(UF6_TYPE, Registration.UF6, Registration.UF6_FLOWING)));
    public static final DeferredHolder<Fluid, Fluid> UF6_FLOWING = FLUIDS.register("uranium_hexafluoride_flowing", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(UF6_TYPE, Registration.UF6, Registration.UF6_FLOWING)));

    public static final DeferredHolder<Fluid, Fluid> HNO3 = FLUIDS.register("nitric_acid", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Source(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(HNO3_TYPE, Registration.HNO3, Registration.HNO3_FLOWING)));
    public static final DeferredHolder<Fluid, Fluid> HNO3_FLOWING = FLUIDS.register("nitric_acid_flowing", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(HNO3_TYPE, Registration.HNO3, Registration.HNO3_FLOWING)));

    public static final DeferredHolder<Fluid, Fluid> TBP = FLUIDS.register("tbp_kerosene", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Source(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(TBP_TYPE, Registration.TBP, Registration.TBP_FLOWING)));
    public static final DeferredHolder<Fluid, Fluid> TBP_FLOWING = FLUIDS.register("tbp_kerosene_flowing", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(TBP_TYPE, Registration.TBP, Registration.TBP_FLOWING)));
    public static final DeferredBlock<Block> REACTOR_CASING = register("reactor_casing", () -> new Block(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));

    public static final DeferredBlock<ReactorControllerBlock> REACTOR_CONTROLLER = register("reactor_controller", () -> new ReactorControllerBlock(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<com.unnamednuclear.block.SteamTurbineBlock> STEAM_TURBINE = register("steam_turbine", () -> new com.unnamednuclear.block.SteamTurbineBlock(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<com.unnamednuclear.block.HeatExchangerBlock> HEAT_EXCHANGER = register("heat_exchanger", () -> new com.unnamednuclear.block.HeatExchangerBlock(BlockBehaviour.Properties.of().strength(5.0f).sound(SoundType.METAL)));

    public static final DeferredHolder<FluidType, FluidType> STEAM_TYPE = FLUID_TYPES.register("steam", () -> new FluidType(FluidType.Properties.create().density(-100).viscosity(100)));
    public static final DeferredHolder<Fluid, Fluid> STEAM = FLUIDS.register("steam", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Source(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(STEAM_TYPE, Registration.STEAM, Registration.STEAM_FLOWING)));
    public static final DeferredHolder<Fluid, Fluid> STEAM_FLOWING = FLUIDS.register("steam_flowing", () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing(new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(STEAM_TYPE, Registration.STEAM, Registration.STEAM_FLOWING)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> STEAM_TURBINE_BE = BLOCK_ENTITIES.register("steam_turbine", () -> BlockEntityType.Builder.of(com.unnamednuclear.block.SteamTurbineBlockEntity::new, STEAM_TURBINE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> HEAT_EXCHANGER_BE = BLOCK_ENTITIES.register("heat_exchanger", () -> BlockEntityType.Builder.of(com.unnamednuclear.block.HeatExchangerBlockEntity::new, HEAT_EXCHANGER.get()).build(null));
    public static final DeferredBlock<ReactorChannelBlock> FUEL_CHANNEL = register("fuel_channel", () -> new ReactorChannelBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> MODERATOR = register("moderator", () -> new Block(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.STONE)));
    public static final DeferredBlock<ReactorChannelBlock> CONTROL_CHANNEL = register("control_channel", () -> new ReactorChannelBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<ReactorChannelBlock> COOLANT_CHANNEL = register("coolant_channel", () -> new ReactorChannelBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> URANIUM_ORE = register("uranium_ore", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> DEEPSLATE_URANIUM_ORE = register("deepslate_uranium_ore", () -> new Block(BlockBehaviour.Properties.of().strength(4.5f).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> FLUORITE_ORE = register("fluorite_ore", () -> new Block(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));
    public static final DeferredBlock<Block> CENTRIFUGE = register("centrifuge", () -> new com.unnamednuclear.block.CentrifugeBlock(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> CHEMICAL_CONVERTER = register("chemical_converter", () -> new com.unnamednuclear.block.ChemicalConverterBlock(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> SOLVENT_EXTRACTOR = register("solvent_extractor", () -> new com.unnamednuclear.block.SolventExtractorBlock(BlockBehaviour.Properties.of().strength(3.5f).sound(SoundType.METAL)));

    public static final DeferredItem<Item> NUCLEAR_FUEL = ITEMS.register("nuclear_fuel", () -> new NuclearFuelItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CONTROL_ROD_ITEM = ITEMS.register("control_rod", () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.register("raw_uranium", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> YELLOWCAKE = ITEMS.register("yellowcake", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> URANIUM_HEXAFLUORIDE = ITEMS.register("uranium_hexafluoride", () -> new EnrichedItem(new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> URANIUM_TETRAFLUORIDE = ITEMS.register("uranium_tetrafluoride", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> FLUORITE = ITEMS.register("fluorite", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> ENRICHED_URANIUM = ITEMS.register("enriched_uranium", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> DEPLETED_URANIUM = ITEMS.register("depleted_uranium", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> URANIUM_DIOXIDE = ITEMS.register("uranium_dioxide", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> FUEL_PELLET = ITEMS.register("fuel_pellet", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> PLUTONIUM = ITEMS.register("plutonium", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> URANYL_NITRATE = ITEMS.register("uranyl_nitrate", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> PLUTONIUM_NITRATE = ITEMS.register("plutonium_nitrate", () -> new EnrichedItem(new Item.Properties()));
    public static final DeferredItem<Item> FISSION_PRODUCTS = ITEMS.register("fission_products", () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> GUIDEBOOK = ITEMS.register("guidebook", () -> new com.unnamednuclear.item.GuidebookItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<DataComponentType<String>> FUEL_TYPE = DATA_COMPONENTS.register("fuel_type", () -> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    public static final Supplier<DataComponentType<Double>> ENRICHMENT = DATA_COMPONENTS.register("enrichment", () -> DataComponentType.<Double>builder().persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE).build());
    public static final Supplier<DataComponentType<com.unnamednuclear.item.NuclearComposition>> COMPOSITION = DATA_COMPONENTS.register("composition", () -> DataComponentType.<com.unnamednuclear.item.NuclearComposition>builder().persistent(com.unnamednuclear.item.NuclearComposition.CODEC).networkSynchronized(com.unnamednuclear.item.NuclearComposition.STREAM_CODEC).build());

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorControllerBlockEntity>> REACTOR_CONTROLLER_BE = BLOCK_ENTITIES.register("reactor_controller", () -> BlockEntityType.Builder.of(ReactorControllerBlockEntity::new, REACTOR_CONTROLLER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReactorChannelBlockEntity>> REACTOR_CHANNEL_BE = BLOCK_ENTITIES.register("reactor_channel", () -> BlockEntityType.Builder.of(ReactorChannelBlockEntity::new, FUEL_CHANNEL.get(), CONTROL_CHANNEL.get(), COOLANT_CHANNEL.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.unnamednuclear.block.CentrifugeBlockEntity>> CENTRIFUGE_BE = BLOCK_ENTITIES.register("centrifuge", () -> BlockEntityType.Builder.of(com.unnamednuclear.block.CentrifugeBlockEntity::new, CENTRIFUGE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.unnamednuclear.block.ChemicalConverterBlockEntity>> CHEMICAL_CONVERTER_BE = BLOCK_ENTITIES.register("chemical_converter", () -> BlockEntityType.Builder.of(com.unnamednuclear.block.ChemicalConverterBlockEntity::new, CHEMICAL_CONVERTER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<com.unnamednuclear.block.SolventExtractorBlockEntity>> SOLVENT_EXTRACTOR_BE = BLOCK_ENTITIES.register("solvent_extractor", () -> BlockEntityType.Builder.of(com.unnamednuclear.block.SolventExtractorBlockEntity::new, SOLVENT_EXTRACTOR.get()).build(null));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.unnamednuclear.block.ReactorMenu>> REACTOR_MENU = MENU_TYPES.register("reactor_controller", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.unnamednuclear.block.ReactorMenu::new));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.unnamednuclear.block.CentrifugeMenu>> CENTRIFUGE_MENU = MENU_TYPES.register("centrifuge", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.unnamednuclear.block.CentrifugeMenu::new));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.unnamednuclear.block.ChemicalConverterMenu>> CHEMICAL_CONVERTER_MENU = MENU_TYPES.register("chemical_converter", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.unnamednuclear.block.ChemicalConverterMenu::new));
    public static final DeferredHolder<net.minecraft.world.inventory.MenuType<?>, net.minecraft.world.inventory.MenuType<com.unnamednuclear.block.SolventExtractorMenu>> SOLVENT_EXTRACTOR_MENU = MENU_TYPES.register("solvent_extractor", () -> net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(com.unnamednuclear.block.SolventExtractorMenu::new));

    private static <T extends Block> DeferredBlock<T> register(String name, Supplier<T> block) {
        DeferredBlock<T> deferredBlock = BLOCKS.register(name, block);
        ITEMS.register(name, () -> new DescBlockItem(deferredBlock.get(), new Item.Properties()));
        return deferredBlock;
    }
}
