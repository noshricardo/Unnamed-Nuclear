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

@EventBusSubscriber(modid = UnnamedNuclear.MODID, value = Dist.CLIENT)
public class UnnamedNuclearClient {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(Registration.REACTOR_MENU.get(), ReactorScreen::new);
        event.register(Registration.CENTRIFUGE_MENU.get(), CentrifugeScreen::new);
        event.register(Registration.CHEMICAL_CONVERTER_MENU.get(), ChemicalConverterScreen::new);
        event.register(Registration.SOLVENT_EXTRACTOR_MENU.get(), SolventExtractorScreen::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
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
        });
    }
}
