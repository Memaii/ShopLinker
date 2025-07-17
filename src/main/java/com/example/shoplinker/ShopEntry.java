package com.example.shoplinker;

import java.util.UUID;

public class ShopEntry {
    private String name;
    private String description;
    private String url;
    private UUID ownerUUID; // UUID du joueur qui a créé la boutique

    // Constructeur pour la création de nouvelles boutiques
    public ShopEntry(String name, String description, String url, UUID ownerUUID) {
        this.name = name;
        this.description = description;
        this.url = url;
        this.ownerUUID = ownerUUID;
    }

    // Constructeur par défaut (peut être utile pour certaines bibliothèques de sérialisation comme Gson, bien que souvent non strictement nécessaire si les champs sont publics ou ont des setters)
    public ShopEntry() {
        // Constructeur vide nécessaire pour la désérialisation par certaines bibliothèques comme Gson si les champs ne sont pas publics
    }


    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    // Setters (pour la modification via la commande)
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // Vous pouvez ajouter un setter pour ownerUUID si vous imaginez un scénario où cela pourrait changer,
    // mais ce n'est généralement pas le cas.
    // public void setOwnerUUID(UUID ownerUUID) {
    //     this.ownerUUID = ownerUUID;
    // }

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