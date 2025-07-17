package com.example.shoplinker;

import net.minecraft.client.Minecraft; // Kept if you intend to use client-side Minecraft instance details
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

/**
 * Client-side entry point for the ShopLinker mod.
 * This class handles client-specific setup tasks, such as registering configuration screens.
 * It is only loaded on the client side of the game.
 */
@Mod(value = ShopLinker.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ShopLinker.MODID, value = Dist.CLIENT)
public class ShopLinkerClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Constructor for the client-side mod.
     *
     * @param container The mod's container.
     */
    public ShopLinkerClient(ModContainer container) {
        // Register an extension point to allow NeoForge to create a config screen for this mod.
        // This screen can be accessed from the Mods menu in Minecraft.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    /**
     * Event listener for client-specific setup, fired after common setup.
     * This is a good place for client-only initialization like keybinds, renderers, etc.
     *
     * @param event The FMLClientSetupEvent.
     */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("ShopLinker client setup complete."); 
        // Example client-side logging (previously: Minecraft.getInstance().getUser().getName())
    }
}