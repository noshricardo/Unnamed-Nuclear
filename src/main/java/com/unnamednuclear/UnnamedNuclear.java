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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

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
        CREATIVE_MODE_TABS.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
