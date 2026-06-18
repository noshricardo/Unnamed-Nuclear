package com.unnamednuclear;

import com.unnamednuclear.registration.Registration;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import net.minecraft.world.level.block.entity.BlockEntityType;

@Mod(UnnamedNuclear.MODID)
public class UnnamedNuclear {
    public static final String MODID = "unnamednuclear";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.unnamednuclear"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> Registration.REACTOR_CONTROLLER.get().asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                Registration.ITEMS.getEntries().forEach(item -> output.accept(item.get()));
            }).build());

    public UnnamedNuclear(IEventBus modEventBus, ModContainer modContainer) {
        Registration.BLOCKS.register(modEventBus);
        Registration.ITEMS.register(modEventBus);
        Registration.DATA_COMPONENTS.register(modEventBus);
        Registration.RECIPE_SERIALIZERS.register(modEventBus);
        Registration.BLOCK_ENTITIES.register(modEventBus);
        Registration.MENU_TYPES.register(modEventBus);
        Registration.FLUID_TYPES.register(modEventBus);
        Registration.FLUIDS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);
        modEventBus.addListener(this::registerPayloads);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            UnnamedNuclearClient.registerModEvents(modEventBus);
        }

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, Registration.HEAT_EXCHANGER_BE.get(), (be, side) -> {
            com.unnamednuclear.block.HeatExchangerBlockEntity exchanger = (com.unnamednuclear.block.HeatExchangerBlockEntity) be;
            if (side == net.minecraft.core.Direction.UP || side == net.minecraft.core.Direction.DOWN) return exchanger.getPrimaryTank();
            if (side == net.minecraft.core.Direction.NORTH || side == net.minecraft.core.Direction.SOUTH) return exchanger.getWaterTank();
            return exchanger.getSteamTank();
        });
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, Registration.REACTOR_CONTROLLER_BE.get(), (be, side) -> {
            com.unnamednuclear.block.ReactorControllerBlockEntity controller = (com.unnamednuclear.block.ReactorControllerBlockEntity) be;
            if (side == net.minecraft.core.Direction.UP) return controller.getInputTank();
            return controller.getOutputTank();
        });
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, Registration.STEAM_TURBINE_BE.get(), (be, side) -> {
            com.unnamednuclear.block.SteamTurbineBlockEntity turbine = (com.unnamednuclear.block.SteamTurbineBlockEntity) be;
            if (side == net.minecraft.core.Direction.DOWN) return turbine.getWaterTank();
            return turbine.getSteamTank();
        });
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, Registration.STEAM_TURBINE_BE.get(), (be, side) -> ((com.unnamednuclear.block.SteamTurbineBlockEntity)be).getEnergyStorage());
        
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, Registration.CENTRIFUGE_BE.get(), (be, side) -> {
            com.unnamednuclear.block.CentrifugeBlockEntity centrifuge = (com.unnamednuclear.block.CentrifugeBlockEntity) be;
            if (side == net.minecraft.core.Direction.UP) return centrifuge.getInputTank();
            if (side == net.minecraft.core.Direction.NORTH || side == net.minecraft.core.Direction.EAST) return centrifuge.getProductTank();
            return centrifuge.getTailsTank();
        });
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, Registration.CHEMICAL_CONVERTER_BE.get(), (be, side) -> ((com.unnamednuclear.block.ChemicalConverterBlockEntity)be).getFluidTank());
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, Registration.SOLVENT_EXTRACTOR_BE.get(), (be, side) -> ((com.unnamednuclear.block.SolventExtractorBlockEntity)be).getFluidTank());
    }

    private void registerPayloads(final net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent event) {
        final net.neoforged.neoforge.network.registration.PayloadRegistrar registrar = event.registrar(MODID);
        registrar.playToClient(
                com.unnamednuclear.network.SimulationDataSyncPayload.TYPE,
                com.unnamednuclear.network.SimulationDataSyncPayload.STREAM_CODEC,
                com.unnamednuclear.network.NetworkHandler::handleSimulationData
        );
        registrar.playToClient(
                com.unnamednuclear.network.ReactorInteriorSyncPayload.TYPE,
                com.unnamednuclear.network.ReactorInteriorSyncPayload.STREAM_CODEC,
                com.unnamednuclear.network.NetworkHandler::handleReactorInterior
        );
    }
}
