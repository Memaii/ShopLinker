package com.example.shoplinker;

import net.minecraft.client.Minecraft; // Gardé si vous utilisez Minecraft.getInstance() pour des infos client
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

@Mod(value = ShopLinker.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ShopLinker.MODID, value = Dist.CLIENT)
public class ShopLinkerClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    public ShopLinkerClient(ModContainer container) {
        // Enregistre l'écran de configuration du mod (même si la config est vide pour l'instant)
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("ShopLinker client setup complete."); 
        // Ligne d'exemple supprimée ou commentée, gardant un log simple pour la confirmation
        // LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName()); 
    }
}