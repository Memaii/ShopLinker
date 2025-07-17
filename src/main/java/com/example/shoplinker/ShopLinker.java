package com.example.shoplinker;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

// Imports de base de Minecraft/NeoForge
import net.minecraft.core.registries.BuiltInRegistries; // Gardé si potentiellement utilisé ailleurs (ex: BuiltInRegistries.ITEM pour items existants)
import net.minecraft.core.registries.Registries; // Gardé si potentiellement utilisé ailleurs pour des registres
import net.minecraft.network.chat.Component; // Gardé pour les messages de chat (commandes)

// Imports de NeoForge pour les événements du mod
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

// Imports pour la persistance des données (vos classes)
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import java.nio.file.Path;

@Mod(ShopLinker.MODID)
public class ShopLinker {

    public static final String MODID = "shoplinker";
    private static final Logger LOGGER = LogUtils.getLogger(); 

    // Vos instances de managers de boutique (essentiel à votre mod)
    public static ShopManager shopManager;
    public static ShopFileManager shopFileManager;

    public ShopLinker(IEventBus modEventBus, ModContainer modContainer) {
        // Enregistrement de l'événement de configuration commune
        modEventBus.addListener(this::commonSetup);

        // Enregistrement des commandes (essentiel à votre mod)
        NeoForge.EVENT_BUS.register(ShopCommands.class);
        // Enregistrement de cette classe pour les événements serveur (Starting/Stopping)
        NeoForge.EVENT_BUS.register(this); 

        // Enregistrement de la configuration du mod (même si vide, la structure est utile)
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("ShopLinker Common Setup complete.");
        // Toutes les références aux configs et blocs d'exemple sont supprimées d'ici
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server is starting, initializing ShopManager and loading shops...");
        // Assurez-vous d'avoir le bon chemin pour la sauvegarde de vos données
        Path dataDir = event.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR).getParent();
        Path shopsFilePath = dataDir.resolve("shoplinker_shops.json");

        shopFileManager = new ShopFileManager(shopsFilePath);
        shopManager = new ShopManager(shopFileManager.loadShops());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("Server is stopping, saving shops...");
        if (shopManager != null && shopFileManager != null) {
            shopFileManager.saveShops(shopManager.getAllShops());
        }
    }

    // Tous les DeferredRegister et leurs objets d'exemple ont été supprimés
}