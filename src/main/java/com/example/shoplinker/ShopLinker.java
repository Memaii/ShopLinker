package com.example.shoplinker;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

// Core Minecraft/NeoForge imports
import net.minecraft.core.registries.BuiltInRegistries; // Kept for potential future use (e.g., getting existing item IDs)
import net.minecraft.core.registries.Registries; // Kept for potential future use for registries
import net.minecraft.network.chat.Component; // Kept for chat messages (commands)

// NeoForge mod event imports
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

// Imports for data persistence (your custom classes)
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import java.nio.file.Path;

/**
 * Main mod class for ShopLinker.
 * This class serves as the entry point for the mod, handling common setup
 * and server lifecycle events to manage shop data persistence.
 */
@Mod(ShopLinker.MODID)
public class ShopLinker {

    public static final String MODID = "shoplinker";
    private static final Logger LOGGER = LogUtils.getLogger(); 

    // Static instances of our custom managers, accessible throughout the mod.
    public static ShopManager shopManager;
    public static ShopFileManager shopFileManager;

    /**
     * Constructor for the ShopLinker mod.
     * This is where event listeners and configurations are registered.
     *
     * @param modEventBus The mod's event bus.
     * @param modContainer The mod's container.
     */
    public ShopLinker(IEventBus modEventBus, ModContainer modContainer) {
        // Register the common setup event listener
        modEventBus.addListener(this::commonSetup);

        // Register the ShopCommands class to the Forge event bus
        // This ensures our /boutique commands are registered with the server.
        NeoForge.EVENT_BUS.register(ShopCommands.class);
        // Register this instance to the Forge event bus for server lifecycle events (starting/stopping).
        NeoForge.EVENT_BUS.register(this); 

        // Register the mod's configuration specification.
        // Even if empty, this establishes the config file structure.
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    /**
     * Handles common setup events, fired during mod initialization.
     * This is a good place for general initialization tasks that run on both client and server.
     *
     * @param event The FMLCommonSetupEvent.
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ShopLinker Common Setup complete.");
        // All example configuration references have been removed from here.
    }

    /**
     * Event listener for when the server is starting.
     * This is where the ShopManager and ShopFileManager are initialized,
     * and existing shop data is loaded from the JSON file.
     *
     * @param event The ServerStartingEvent.
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server is starting, initializing ShopManager and loading shops...");
        // Determine the correct path for saving mod-specific data.
        // DATAPACK_DIR is typically within the world save folder.
        Path dataDir = event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR).getParent();
        Path shopsFilePath = dataDir.resolve("shoplinker_shops.json");

        shopFileManager = new ShopFileManager(shopsFilePath);
        // Load shops from the file and initialize the ShopManager with them.
        shopManager = new ShopManager(shopFileManager.loadShops());
    }

    /**
     * Event listener for when the server is stopping.
     * This is crucial for saving the current shop data to the JSON file
     * to ensure persistence across server restarts.
     *
     * @param event The ServerStoppingEvent.
     */
    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Server is stopping, saving shops...");
        // Ensure both managers are initialized before attempting to save.
        if (shopManager != null && shopFileManager != null) {
            shopFileManager.saveShops(shopManager.getAllShops());
        }
    }

    // All DeferredRegister and example objects have been removed from this class.
}