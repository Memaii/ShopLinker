package com.example.shoplinker;

// Import nécessaire pour ModConfigSpec
import net.neoforged.neoforge.common.ModConfigSpec;

// La classe Config est maintenant minimale. Si vous n'avez aucune configuration,
// vous pourriez même la supprimer et la référence dans ShopLinker, mais c'est une bonne base.
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Toutes les définitions de configuration des exemples ont été supprimées.
    // Laissez cette classe vide à part le SPEC si vous n'avez pas de configs spécifiques.

    static final ModConfigSpec SPEC = BUILDER.build(); 
}