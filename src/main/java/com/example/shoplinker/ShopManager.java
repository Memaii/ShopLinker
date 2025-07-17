package com.example.shoplinker;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Manages the collection of ShopEntry objects, providing methods for
 * adding, removing, updating, and retrieving shop data.
 * This class acts as the central point for interacting with shop data in memory.
 */
public class ShopManager {
    private static final Logger LOGGER = LogUtils.getLogger(); // Own LOGGER for ShopManager
    private List<ShopEntry> shops;
    private final ShopFileManager fileManager; // Reference to the file manager for persistence

    /**
     * Constructs a ShopManager.
     * Initializes the list of shops with an initial set of data (e.g., loaded from file at startup).
     *
     * @param initialShops The list of shops loaded at startup.
     */
    public ShopManager(List<ShopEntry> initialShops) {
        // Create a copy to prevent direct external modifications to the internal list
        this.shops = new ArrayList<>(initialShops); 
        // Ensure ShopLinker.shopFileManager is initialized before this constructor is called.
        this.fileManager = ShopLinker.shopFileManager; 
    }

    /**
     * Internal helper method to trigger a save operation via the file manager.
     * This ensures that any changes to the shops list are persisted to disk.
     */
    private void save() {
        if (fileManager != null) {
            fileManager.saveShops(this.shops);
        } else {
            // This warning indicates an initialization order issue if it occurs
            LOGGER.warn("ShopFileManager is null, unable to save shops!"); 
        }
    }

    /**
     * Adds a new shop entry to the manager.
     *
     * @param shop The ShopEntry object to add.
     * @return true if the shop was added, false if a shop with the same name already exists.
     */
    public boolean addShop(ShopEntry shop) {
        // Check for duplicate shop names (case-insensitive)
        if (getShopByName(shop.getName()).isPresent()) {
            return false; // Shop with this name already exists
        }
        this.shops.add(shop);
        save(); // Save after adding
        return true;
    }

    /**
     * Removes a shop entry by its name.
     *
     * @param name The name of the shop to remove.
     * @return true if the shop was found and removed, false otherwise.
     */
    public boolean removeShop(String name) {
        boolean removed = this.shops.removeIf(shop -> shop.getName().equalsIgnoreCase(name));
        if (removed) {
            save(); // Save after removal
        }
        return removed;
    }

    /**
     * Triggers a save operation after an existing ShopEntry object has been modified externally.
     * Since modifications are done directly on the ShopEntry object retrieved from the list,
     * this method ensures the updated state is persisted.
     *
     * @param shop The modified ShopEntry object.
     * @return true (assuming the object was successfully modified elsewhere).
     */
    public boolean updateShop(ShopEntry shop) {
        // The ShopEntry object in the 'shops' list is modified directly by setters
        // called from the command handler (ShopCommands::modifyShop).
        // Therefore, we just need to ensure the data is saved to file.
        save(); // Save after an update
        return true; 
    }

    /**
     * Retrieves a shop entry by its name.
     *
     * @param name The name of the shop to retrieve.
     * @return An Optional containing the ShopEntry if found, or an empty Optional if not found.
     */
    public Optional<ShopEntry> getShopByName(String name) {
        return this.shops.stream()
                .filter(shop -> shop.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Gets an unmodifiable list of all registered shops.
     *
     * @return An unmodifiable List of ShopEntry objects.
     */
    public List<ShopEntry> getAllShops() {
        return Collections.unmodifiableList(shops); 
    }
}