package com.example.shoplinker;

// Required import for ModConfigSpec
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * This class handles the mod's configuration specifications using NeoForge's ModConfigSpec.
 * It's currently minimal as no custom configuration options have been defined yet.
 * This class provides the structure for a config file to be generated if needed in the future.
 */
public class Config {
    // Builder for creating configuration entries
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // The ModConfigSpec instance, which is built from the BUILDER.
    // This SPEC is registered with the mod container in the main mod class.
    static final ModConfigSpec SPEC = BUILDER.build(); 
}