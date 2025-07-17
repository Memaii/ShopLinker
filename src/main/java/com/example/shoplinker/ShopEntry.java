package com.example.shoplinker;

import java.util.UUID;

/**
 * Represents a single shop entry, storing its name, description, URL, and owner's UUID.
 * This class is used for serialization and deserialization of shop data.
 */
public class ShopEntry {
    private String name;
    private String description;
    private String url;
    private UUID ownerUUID; // UUID of the player who created the shop

    /**
     * Constructor for creating new shop entries.
     *
     * @param name The unique name of the shop.
     * @param description A short description of the shop.
     * @param url An optional URL associated with the shop (can be empty).
     * @param ownerUUID The UUID of the player who owns this shop.
     */
    public ShopEntry(String name, String description, String url, UUID ownerUUID) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.ownerUUID = ownerUUID;
    }

    /**
     * Default constructor.
     * Useful for deserialization by libraries like Gson, even if not strictly necessary
     * when fields are public or have setters.
     */
    public ShopEntry() {
        // Empty constructor needed for deserialization by libraries like Gson if fields are not public
    }

    // --- Getters ---

    /**
     * Gets the name of the shop.
     * @return The shop's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the description of the shop.
     * @return The shop's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the URL associated with the shop.
     * @return The shop's URL.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the UUID of the shop's owner.
     * @return The owner's UUID.
     */
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // --- Setters (for modification via commands) ---

    /**
     * Sets a new name for the shop.
     * @param name The new name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets a new description for the shop.
     * @param description The new description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets a new URL for the shop.
     * @param url The new URL.
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ShopEntry{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", url='" + url + '\'' +
               ", ownerUUID=" + ownerUUID +
               '}';
    }
}