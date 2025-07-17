package com.example.shoplinker;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ShopManager {
    private static final Logger LOGGER = LogUtils.getLogger(); // Son propre LOGGER
    private List<ShopEntry> shops;
    private final ShopFileManager fileManager; // Référence au gestionnaire de fichiers

    // Constructeur : reçoit la liste de boutiques chargée au démarrage et une référence au fileManager
    public ShopManager(List<ShopEntry> initialShops) {
        this.shops = new ArrayList<>(initialShops); // Crée une copie pour éviter les modifications externes directes
        this.fileManager = ShopLinker.shopFileManager; // Assurez-vous que ShopLinker.shopFileManager est initialisé avant d'appeler ce constructeur
    }

    // Méthode interne pour sauvegarder après chaque modification
    private void save() {
        if (fileManager != null) {
            fileManager.saveShops(this.shops);
        } else {
            // Ceci ne devrait pas arriver si les managers sont bien initialisés au démarrage du serveur
            LOGGER.warn("ShopFileManager est null, impossible de sauvegarder les boutiques !"); // Utilise son propre LOGGER
        }
    }

    public boolean addShop(ShopEntry shop) {
        if (getShopByName(shop.getName()).isPresent()) {
            return false; // Shop with this name already exists
        }
        this.shops.add(shop);
        save(); // Sauvegarde après ajout
        return true;
    }

    public boolean removeShop(String name) {
        boolean removed = this.shops.removeIf(shop -> shop.getName().equalsIgnoreCase(name));
        if (removed) {
            save(); // Sauvegarde après suppression
        }
        return removed;
    }

    // La modification est gérée "en place" sur l'objet ShopEntry,
    // donc il suffit d'appeler save() après la modification si elle a réussi.
    public boolean updateShop(ShopEntry shop) {
        // Dans notre implémentation actuelle, la modification de l'objet ShopEntry se fait directement,
        // donc il suffit de s'assurer que l'appelant appelle save() après les setters.
        // Ou bien, si nous voulions une méthode update plus "formelle", nous pourrions la faire ici.
        // Pour l'instant, comme ShopCommands::modifyShop modifie l'objet directement,
        // nous nous fions à son appel à save().
        save(); // Sauvegarde après mise à jour (l'objet est déjà modifié)
        return true; // Supposons toujours le succès si l'objet a été trouvé et modifié
    }


    public Optional<ShopEntry> getShopByName(String name) {
        return this.shops.stream()
                .filter(shop -> shop.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<ShopEntry> getAllShops() {
        return Collections.unmodifiableList(shops); // Return an unmodifiable list to prevent external modification
    }
}