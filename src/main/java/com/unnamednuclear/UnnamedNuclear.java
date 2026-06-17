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
        Registration.BLOCK_ENTITIES.register(modEventBus);
        Registration.MENU_TYPES.register(modEventBus);
        Registration.FLUID_TYPES.register(modEventBus);
        Registration.FLUIDS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerCapabilities);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK, (BlockEntityType<com.unnamednuclear.block.HeatExchangerBlockEntity>)Registration.HEAT_EXCHANGER_BE.get(), (be, side) -> {
            // Return one of the tanks based on side or just a wrapper
            return null; // For now
        });
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK, (BlockEntityType<com.unnamednuclear.block.SteamTurbineBlockEntity>)Registration.STEAM_TURBINE_BE.get(), (be, side) -> null);
    }
}
