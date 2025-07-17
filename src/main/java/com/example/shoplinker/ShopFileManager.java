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

/**
 * Manages the loading and saving of shop data to and from a JSON file.
 * Uses Gson library for JSON serialization/deserialization.
 */
public class ShopFileManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Gson gson;
    private final Path filePath;

    /**
     * Constructs a ShopFileManager with a specific file path.
     *
     * @param filePath The path to the JSON file where shop data will be stored.
     */
    public ShopFileManager(Path filePath) {
        this.filePath = filePath;
        // Configure Gson for pretty printing (readable JSON)
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Loads the list of shops from the JSON file.
     * Creates a new empty list if the file does not exist or if an error occurs during loading.
     *
     * @return The loaded list of shops, or an empty list if the file doesn't exist or on error.
     */
    public List<ShopEntry> loadShops() {
        // If the file does not exist, return an empty list
        if (!filePath.toFile().exists()) {
            LOGGER.info("Shop data file does not exist, creating new list: {}", filePath);
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(filePath.toFile())) {
            // Define the type for Gson to correctly deserialize a List of ShopEntry objects
            Type listType = new TypeToken<ArrayList<ShopEntry>>(){}.getType();
            List<ShopEntry> shops = gson.fromJson(reader, listType);
            LOGGER.info("Loaded {} shops from {}", shops != null ? shops.size() : 0, filePath);
            // Return the loaded shops or an empty list if deserialization results in null
            return shops != null ? shops : new ArrayList<>();
        } catch (IOException e) {
            LOGGER.error("Failed to load shops from {}: {}", filePath, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Saves the list of shops to the JSON file.
     * Creates the parent directory if it does not exist.
     *
     * @param shops The list of shops to save.
     * @return true if saving was successful, false otherwise.
     */
    public boolean saveShops(List<ShopEntry> shops) {
        try {
            // Ensure the parent directory exists before writing the file
            if (filePath.getParent() != null) {
                filePath.getParent().toFile().mkdirs();
            }

            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                gson.toJson(shops, writer); // Serialize the list of shops to JSON
                LOGGER.info("Saved {} shops to {}", shops.size(), filePath);
                return true;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save shops to {}: {}", filePath, e.getMessage());
            return false;
        }
    }
}