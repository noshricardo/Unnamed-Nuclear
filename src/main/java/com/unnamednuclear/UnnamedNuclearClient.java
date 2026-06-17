package com.unnamednuclear;

import com.unnamednuclear.block.*;
import com.unnamednuclear.registration.Registration;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import com.unnamednuclear.client.NuclearVisualizationRenderer;
import com.unnamednuclear.client.ReactorOverlayRenderer;

@EventBusSubscriber(modid = UnnamedNuclear.MODID, value = Dist.CLIENT)
public class UnnamedNuclearClient {
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        NuclearVisualizationRenderer.onRenderLevelStage(event);
        ReactorOverlayRenderer.onRenderLevelStage(event);
    }

    public static void registerModEvents(net.neoforged.bus.api.IEventBus modEventBus) {
        modEventBus.addListener(UnnamedNuclearClient::registerScreens);
        modEventBus.addListener(UnnamedNuclearClient::onClientSetup);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.REACTOR_MENU.get(), ReactorScreen::new);
        event.register(Registration.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
        event.register(Registration.CHEMICAL_CONVERTER_MENU.get(), ChemicalConverterScreen::new);
        event.register(Registration.SOLVENT_EXTRACTOR_MENU.get(), SolventExtractorScreen::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(Registration.HF.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.HF_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.F2.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.F2_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.UF6.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.UF6_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.HNO3.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.HNO3_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.TBP.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.TBP_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.STEAM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.STEAM_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.SODIUM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.SODIUM_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.HOT_SODIUM.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(Registration.HOT_SODIUM_FLOWING.get(), RenderType.translucent());
        });
    }
}
