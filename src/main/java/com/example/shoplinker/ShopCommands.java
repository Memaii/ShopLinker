package com.example.shoplinker; // Declares the package for the ShopCommands class.

import com.mojang.brigadier.CommandDispatcher; // Imports CommandDispatcher for registering commands.
import com.mojang.brigadier.arguments.StringArgumentType; // Imports StringArgumentType for string command arguments.
import com.mojang.brigadier.context.CommandContext; // Imports CommandContext for command execution context.
import com.mojang.brigadier.exceptions.CommandSyntaxException; // Imports CommandSyntaxException for handling command syntax errors.
import net.minecraft.commands.CommandSourceStack; // Imports CommandSourceStack for accessing command source information.
import net.minecraft.commands.Commands; // Imports Commands for command literal and argument helpers.
import net.minecraft.network.chat.ClickEvent; // Imports ClickEvent for handling clickable text events.
import net.minecraft.network.chat.Component; // Imports Component for creating translatable text.
import net.minecraft.network.chat.MutableComponent; // Imports MutableComponent for modifiable text components.
import net.minecraft.network.chat.Style; // Imports Style for applying styles to text components.
import net.neoforged.bus.api.SubscribeEvent; // Imports SubscribeEvent for event bus subscriptions.
import net.neoforged.neoforge.event.RegisterCommandsEvent; // Imports RegisterCommandsEvent for command registration.

import java.util.List; // Imports List for handling collections of shop entries.
import java.util.Optional; // Imports Optional for handling nullable shop entries.
import java.util.UUID; // Imports UUID for unique player identification.

import com.mojang.logging.LogUtils; // Imports LogUtils for logging utilities.
import org.slf4j.Logger; // Imports Logger for logging messages.
import net.minecraft.ChatFormatting; // Imports ChatFormatting for chat colors.

/**
 * This class handles the registration and execution of in-game shop commands.
 * It uses the Brigadier command library for command parsing and execution.
 */
public class ShopCommands {

    // Logger for logging messages and debugging information.
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * This method registers all the shop-related commands when the RegisterCommandsEvent is fired.
     * It sets up the command structure, including subcommands, arguments, and permission checks.
     *
     * @param event The RegisterCommandsEvent containing the command dispatcher.
     */
    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher(); // Get the command dispatcher.

        // Register the base "shop" command.
        dispatcher.register(
            Commands.literal("shop") // Defines the base command "/shop".
                .requires(source -> source.hasPermission(0)) // Requires a minimum permission level of 0 (anyone can use).
                // Subcommand for listing shops: "/shop list"
                .then(Commands.literal("list")
                    .executes(ShopCommands::listShops) // Executes the listShops method when "/shop list" is run.
                )
                // Subcommand for adding a shop: "/shop add <name> <description> [url]"
                .then(Commands.literal("add")
                    .requires(source -> source.hasPermission(0)) // Requires a minimum permission level of 0.
                    .then(Commands.argument("name", StringArgumentType.string()) // Defines a string argument for shop name.
                        .then(Commands.argument("description", StringArgumentType.string()) // Defines a string argument for shop description.
                            .executes(ShopCommands::addShop) // Executes addShop without a URL.
                            .then(Commands.argument("url", StringArgumentType.greedyString()) // Defines a greedy string argument for URL (optional).
                                .executes(ShopCommands::addShop) // Executes addShop with a URL.
                            )
                        )
                    )
                )
                // Subcommand for removing a shop: "/shop remove <name>"
                .then(Commands.literal("remove")
                    .requires(source -> source.hasPermission(0)) // Requires a minimum permission level of 0.
                    .then(Commands.argument("name", StringArgumentType.string()) // Defines a string argument for shop name to remove.
                        .executes(ShopCommands::removeShop) // Executes the removeShop method.
                    )
                )
                // Subcommand for modifying a shop: "/shop modify <shopName> <field> <newValue>"
                .then(Commands.literal("modify")
                    .requires(source -> source.hasPermission(0)) // Requires a minimum permission level of 0.
                    .then(Commands.argument("shopName", StringArgumentType.string()) // Defines a string argument for the shop to modify.
                        // Sub-subcommand to modify shop name: "/shop modify <shopName> name <newName>"
                        .then(Commands.literal("name")
                            .then(Commands.argument("newName", StringArgumentType.string()) // Defines a string argument for the new name.
                                .executes(context -> modifyShop(context, "name")) // Calls modifyShop for name modification.
                            )
                        )
                        // Sub-subcommand to modify shop description: "/shop modify <shopName> description <newDescription>"
                        .then(Commands.literal("description")
                            .then(Commands.argument("newDescription", StringArgumentType.string()) // Defines a string argument for the new description.
                                .executes(context -> modifyShop(context, "description")) // Calls modifyShop for description modification.
                            )
                        )
                        // Sub-subcommand to modify shop URL: "/shop modify <shopName> url <newUrl>"
                        .then(Commands.literal("url")
                            .then(Commands.argument("newUrl", StringArgumentType.greedyString()) // Defines a greedy string argument for the new URL.
                                .executes(context -> modifyShop(context, "url")) // Calls modifyShop for URL modification.
                            )
                        )
                    )
                )
                // NEW Subcommand: /shop info <name>
                .then(Commands.literal("info")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ShopCommands::shopInfo) // Execute shopInfo method
                    )
                )
        );
    }

    /**
     * Executes the "/shop list" command. It retrieves all registered shops
     * and sends a formatted list to the command source (player or console).
     * Includes clickable links for shop URLs if they are valid.
     *
     * @param context The command context.
     * @return 1 if successful, 0 if no shops are found.
     * @throws CommandSyntaxException If there's an issue with command syntax (though unlikely for this command).
     */
    private static int listShops(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource(); // Get the source of the command.
        List<ShopEntry> shops = ShopLinker.shopManager.getAllShops(); // Retrieve all shop entries from the shop manager.

        // Check if there are no shops registered.
        if (shops.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.shoplinker.generic.no_shops"), false); // Send "no shops" message.
            return 0; // Indicate failure (no shops found).
        }

        source.sendSuccess(() -> Component.translatable("commands.shoplinker.list.title"), false); // Send the list title.
        // Iterate through each shop and format its display.
        for (ShopEntry shop : shops) {
            String shopName = shop.getName(); // Get shop name.
            String shopDescription = shop.getDescription(); // Get shop description.
            String shopUrl = shop.getUrl(); // Get shop URL.

            // Create a base component for the shop name and description.
            MutableComponent shopComponent = Component.literal(" - §b" + shopName + "§r: §f" + shopDescription + " ");

            // Check if the URL is not empty and starts with http:// or https:// to make it clickable.
            if (!shopUrl.isEmpty() && (shopUrl.startsWith("http://") || shopUrl.startsWith("https://"))) {
                // Create a clickable URL component.
                MutableComponent urlComponent = Component.literal("§9[Lien]") // Display "[Link]".
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, shopUrl)) // Set click event to open URL.
                        .withUnderlined(true)); // Underline the text.
                source.sendSuccess(() -> shopComponent.append(urlComponent), false); // Append and send the shop component with the link.
            } else {
                // If URL is invalid or empty, send the shop component with a "no link" message.
                source.sendSuccess(() -> shopComponent.append(Component.translatable("commands.shoplinker.list.no_link")), false);
            }
        }
        return 1; // Indicate success.
    }

    /**
     * Executes the "/shop add" command. It attempts to add a new shop
     * with the given name, description, and an optional URL.
     * It validates the URL format and checks for existing shop names.
     *
     * @param context The command context.
     * @return 1 if the shop is added successfully, 0 otherwise.
     * @throws CommandSyntaxException If the player cannot be determined from the source.
     */
    private static int addShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource(); // Get the source of the command.
        String shopName = StringArgumentType.getString(context, "name"); // Get the shop name from arguments.
        String shopDescription = StringArgumentType.getString(context, "description"); // Get the shop description from arguments.
        String shopUrl = ""; // Initialize URL as empty.

        try {
            shopUrl = StringArgumentType.getString(context, "url"); // Attempt to get the URL argument.
        } catch (IllegalArgumentException e) {
            // URL not provided, shopUrl remains empty, which is handled later.
        }

        // Debug logging for URL validation.
        LOGGER.info("Debug: URL received: '{}'", shopUrl);
        LOGGER.info("Debug: URL is empty: {}", shopUrl.isEmpty());
        LOGGER.info("Debug: URL starts with http:// : {}", shopUrl.startsWith("http://"));
        LOGGER.info("Debug: URL starts with https:// : {}", shopUrl.startsWith("https://"));

        // Validate the URL format if it's not empty.
        if (!shopUrl.isEmpty() && !(shopUrl.startsWith("http://") || shopUrl.startsWith("https://"))) {
            source.sendFailure(Component.translatable("commands.shoplinker.add.invalid_url")); // Send invalid URL message.
            return 0; // Indicate failure.
        }

        UUID playerUUID = source.getPlayerOrException().getUUID(); // Get the UUID of the player who executed the command.

        ShopEntry newShop = new ShopEntry(shopName, shopDescription, shopUrl, playerUUID); // Create a new ShopEntry object.
        // Attempt to add the shop using the shop manager.
        if (ShopLinker.shopManager.addShop(newShop)) {
            source.sendSuccess(() -> Component.translatable("commands.shoplinker.add.success", shopName), true); // Send success message.
        } else {
            source.sendFailure(Component.translatable("commands.shoplinker.add.exists", shopName)); // Send "shop already exists" message.
        }
        return 1; // Indicate success or that an attempt was made.
    }

    /**
     * Executes the "/shop remove" command. It removes a shop with the given name.
     * It performs permission checks, ensuring either an OP player or the shop owner
     * is attempting to remove the shop.
     *
     * @param context The command context.
     * @return 1 if the shop is removed successfully, 0 otherwise.
     * @throws CommandSyntaxException If the player cannot be determined from the source.
     */
    private static int removeShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource(); // Get the source of the command.
        String shopName = StringArgumentType.getString(context, "name"); // Get the name of the shop to remove.

        Optional<ShopEntry> shopToRemove = ShopLinker.shopManager.getShopByName(shopName); // Try to find the shop by name.

        // Check if the shop exists.
        if (shopToRemove.isPresent()) {
            ShopEntry shop = shopToRemove.get(); // Get the ShopEntry object.
            // Check if the command source has permission level 2 (OP) OR is the owner of the shop.
            if (source.hasPermission(2) || source.getPlayerOrException().getUUID().equals(shop.getOwnerUUID())) {
                // Attempt to remove the shop.
                if (ShopLinker.shopManager.removeShop(shopName)) {
                    source.sendSuccess(() -> Component.translatable("commands.shoplinker.remove.success", shopName), true); // Send success message.
                } else {
                    source.sendFailure(Component.translatable("commands.shoplinker.remove.not_found", shopName)); // Should not happen if shopToRemove.isPresent() is true.
                }
            } else {
                source.sendFailure(Component.translatable("commands.shoplinker.remove.permission")); // Send permission denied message.
            }
        } else {
            source.sendFailure(Component.translatable("commands.shoplinker.remove.not_found", shopName)); // Send "shop not found" message.
        }
        return 1; // Indicate success or that an attempt was made.
    }

    /**
     * Executes the "/shop modify" command. It allows modification of a shop's
     * name, description, or URL. It performs permission checks (OP or shop owner)
     * and validates the new values (e.g., URL format, uniqueness of new name).
     *
     * @param context The command context.
     * @param fieldToModify A string indicating which field to modify ("name", "description", or "url").
     * @return 1 if the shop is modified successfully, 0 otherwise.
     * @throws CommandSyntaxException If the player cannot be determined from the source.
     */
    private static int modifyShop(CommandContext<CommandSourceStack> context, String fieldToModify) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource(); // Get the source of the command.
        String oldShopName = StringArgumentType.getString(context, "shopName"); // Get the name of the shop to modify.

        Optional<ShopEntry> optionalShop = ShopLinker.shopManager.getShopByName(oldShopName); // Try to find the shop by its old name.

        // Check if the shop exists.
        if (optionalShop.isEmpty()) {
            source.sendFailure(Component.translatable("commands.shoplinker.modify.not_found", oldShopName)); // Send "shop not found" message.
            return 0; // Indicate failure.
        }

        ShopEntry shopToModify = optionalShop.get(); // Get the ShopEntry object.
        UUID playerUUID = source.getPlayerOrException().getUUID(); // Get the UUID of the player who executed the command.

        // Check if the command source has permission level 2 (OP) AND is NOT the owner of the shop.
        // If neither is true, permission is denied.
        if (!source.hasPermission(2) && !playerUUID.equals(shopToModify.getOwnerUUID())) {
            source.sendFailure(Component.translatable("commands.shoplinker.generic.no_permission")); // Send permission denied message.
            return 0; // Indicate failure.
        }

        boolean modified = false; // Flag to track if a modification occurred.
        String tempNewValue = ""; // Temporary variable to hold the new value.

        // Use a switch statement to handle different fields to modify.
        switch (fieldToModify) {
            case "name":
                tempNewValue = StringArgumentType.getString(context, "newName"); // Get the new name.
                // Check if a shop with the new name already exists AND it's not the same as the old name.
                if (ShopLinker.shopManager.getShopByName(tempNewValue).isPresent() && !tempNewValue.equalsIgnoreCase(oldShopName)) {
                    source.sendFailure(Component.translatable("commands.shoplinker.modify.name_exists", tempNewValue)); // Send "name already exists" message.
                    return 0; // Indicate failure.
                }
                shopToModify.setName(tempNewValue); // Set the new name.
                modified = true; // Mark as modified.
                break;
            case "description":
                tempNewValue = StringArgumentType.getString(context, "newDescription"); // Get the new description.
                shopToModify.setDescription(tempNewValue); // Set the new description.
                modified = true; // Mark as modified.
                break;
            case "url":
                tempNewValue = StringArgumentType.getString(context, "newUrl"); // Get the new URL.
                // Validate the new URL format if it's not empty.
                if (!tempNewValue.isEmpty() && !(tempNewValue.startsWith("http://") || tempNewValue.startsWith("https://"))) {
                    source.sendFailure(Component.translatable("commands.shoplinker.add.invalid_url")); // Send invalid URL message.
                    return 0; // Indicate failure.
                }
                shopToModify.setUrl(tempNewValue); // Set the new URL.
                modified = true; // Mark as modified.
                break;
        }

        // If a modification occurred, update the shop and send a success message.
        if (modified) {
            final String finalNewValue = tempNewValue; // Final variable for lambda expression.

            source.sendSuccess(() -> Component.translatable("commands.shoplinker.modify.success", oldShopName, fieldToModify, finalNewValue), true); // Send success message with details.
            ShopLinker.shopManager.updateShop(shopToModify); // Update the shop in the shop manager.
        } else {
            source.sendFailure(Component.translatable("commands.shoplinker.generic.error")); // Send a generic error if no modification happened (should not be reached with current logic).
        }

        return 1; // Indicate success or that an attempt was made.
    }

    /**
     * Executes the "/shop info" command. It displays detailed information about a specific shop.
     * The owner UUID is only displayed to players with a permission level of 2 (OP) or higher.
     *
     * @param context The command context.
     * @return 1 if shop information is displayed successfully, 0 otherwise.
     */
    private static int shopInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "name");

        Optional<ShopEntry> shopOpt = ShopLinker.shopManager.getShopByName(name);

        if (shopOpt.isPresent()) {
            ShopEntry shop = shopOpt.get();
            source.sendSuccess(() -> Component.translatable("commands.shoplinker.info.header", shop.getName()).withStyle(ChatFormatting.GOLD), false);
            source.sendSuccess(() -> Component.translatable("commands.shoplinker.info.description", shop.getDescription()).withStyle(ChatFormatting.WHITE), false);
            if (!shop.getUrl().isEmpty()) {
                source.sendSuccess(() -> Component.translatable("commands.shoplinker.info.url", shop.getUrl())
                        .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.AQUA)
                            .withUnderlined(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, shop.getUrl()))
                        ), false);
            } else {
                source.sendSuccess(() -> Component.translatable("commands.shoplinker.info.no_url").withStyle(ChatFormatting.GRAY), false);
            }

            // Only send owner UUID if the player has permission level 2 or higher
            if (source.hasPermission(2)) {
                source.sendSuccess(() -> Component.translatable("commands.shoplinker.info.owner", shop.getOwnerUUID().toString()).withStyle(ChatFormatting.GRAY), false);
            }

        } else {
            source.sendFailure(Component.translatable("commands.shoplinker.info.not_found", name));
        }
        return 1;
    }
}