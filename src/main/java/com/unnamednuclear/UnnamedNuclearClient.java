package com.unnamednuclear;

import com.unnamednuclear.block.ReactorScreen;
import com.unnamednuclear.block.CentrifugeScreen;
import com.unnamednuclear.client.ReactorOverlayRenderer;
import com.unnamednuclear.registration.Registration;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = UnnamedNuclear.MODID, dist = Dist.CLIENT)
public class UnnamedNuclearClient {
    public UnnamedNuclearClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::registerScreens);
        NeoForge.EVENT_BUS.addListener(ReactorOverlayRenderer::onRenderLevelStage);
    }

    public void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.REACTOR_MENU.get(), ReactorScreen::new);
        event.register(Registration.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
    }
}
