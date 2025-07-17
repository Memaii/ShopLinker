package com.example.shoplinker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ShopFileManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson gson;
    private final Path filePath;

    public ShopFileManager(Path filePath) {
        this.filePath = filePath;
        // Configuration de Gson pour une jolie impression (pretty printing) et pour la gestion des UUID
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Charge la liste des boutiques à partir du fichier JSON.
     * @return La liste des boutiques chargées, ou une liste vide si le fichier n'existe pas ou s'il y a une erreur.
     */
    public List<ShopEntry> loadShops() {
        if (!filePath.toFile().exists()) {
            LOGGER.info("Shop data file does not exist, creating new list: {}", filePath);
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(filePath.toFile())) {
            Type listType = new TypeToken<ArrayList<ShopEntry>>(){}.getType();
            List<ShopEntry> shops = gson.fromJson(reader, listType);
            LOGGER.info("Loaded {} shops from {}", shops != null ? shops.size() : 0, filePath);
            return shops != null ? shops : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.error("Failed to load shops from {}: {}", filePath, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Sauvegarde la liste des boutiques dans le fichier JSON.
     * Crée le répertoire parent si nécessaire.
     * @param shops La liste des boutiques à sauvegarder.
     * @return true si la sauvegarde a réussi, false sinon.
     */
    public boolean saveShops(List<ShopEntry> shops) {
        try {
            // Assurez-vous que le répertoire parent existe
            if (filePath.getParent() != null) {
                filePath.getParent().toFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                gson.toJson(shops, writer);
                LOGGER.info("Saved {} shops to {}", shops.size(), filePath);
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save shops to {}: {}", filePath, e.getMessage());
            return false;
        }
    }
}