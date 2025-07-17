package com.example.shoplinker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger; // Garder cet import si vous utilisez LOGGER.info, sinon il peut être supprimé.

public class ShopCommands {

    // Si vous utilisez le LOGGER de ShopLinker, cette ligne peut être supprimée.
    // Sinon, gardez-la. Pour ce tutoriel, on va la garder pour la clarté.
    private static final Logger LOGGER = LogUtils.getLogger(); 

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("shop")
                .requires(source -> source.hasPermission(0))
                .then(Commands.literal("list")
                    .executes(ShopCommands::listShops)
                )
                .then(Commands.literal("add")
                    .requires(source -> source.hasPermission(0))
                    .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("description", StringArgumentType.string())
                            .executes(ShopCommands::addShop)
                            .then(Commands.argument("url", StringArgumentType.greedyString())
                                .executes(ShopCommands::addShop)
                            )
                        )
                    )
                )
                .then(Commands.literal("remove")
                    .requires(source -> source.hasPermission(0))
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(ShopCommands::removeShop)
                    )
                )
                .then(Commands.literal("modify")
                    .requires(source -> source.hasPermission(0))
                    .then(Commands.argument("shopName", StringArgumentType.string())
                        .then(Commands.literal("name")
                            .then(Commands.argument("newName", StringArgumentType.string())
                                .executes(context -> modifyShop(context, "name"))
                            )
                        )
                        .then(Commands.literal("description")
                            .then(Commands.argument("newDescription", StringArgumentType.string())
                                .executes(context -> modifyShop(context, "description"))
                            )
                        )
                        .then(Commands.literal("url")
                            .then(Commands.argument("newUrl", StringArgumentType.greedyString())
                                .executes(context -> modifyShop(context, "url"))
                            )
                        )
                    )
                )
        );
    }

    private static int listShops(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        List<ShopEntry> shops = ShopLinker.shopManager.getAllShops();

        if (shops.isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.shoplinker.generic.no_shops"), false);
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("commands.shoplinker.list.title"), false);
        for (ShopEntry shop : shops) {
            String shopName = shop.getName();
            String shopDescription = shop.getDescription();
            String shopUrl = shop.getUrl();

            MutableComponent shopComponent = Component.literal(" - §b" + shopName + "§r: §f" + shopDescription + " ");

            if (!shopUrl.isEmpty() && (shopUrl.startsWith("http://") || shopUrl.startsWith("https://"))) {
                MutableComponent urlComponent = Component.literal("§9[Lien]")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, shopUrl))
                        .withUnderlined(true));
                source.sendSuccess(() -> shopComponent.append(urlComponent), false);
            } else {
                source.sendSuccess(() -> shopComponent.append(Component.translatable("commands.shoplinker.list.no_link")), false);
            }
        }
        return 1;
    }

    private static int addShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String shopName = StringArgumentType.getString(context, "name");
        String shopDescription = StringArgumentType.getString(context, "description");
        String shopUrl = "";

        try {
            shopUrl = StringArgumentType.getString(context, "url");
        } catch (IllegalArgumentException e) {
            // URL not provided, remains empty.
        }

        // Les logs peuvent rester en anglais ou être traduits si nécessaire pour le développement,
        // mais ils ne sont pas affichés aux joueurs.
        LOGGER.info("Debug: URL received: '{}'", shopUrl);
        LOGGER.info("Debug: URL is empty: {}", shopUrl.isEmpty());
        LOGGER.info("Debug: URL starts with http:// : {}", shopUrl.startsWith("http://"));
        LOGGER.info("Debug: URL starts with https:// : {}", shopUrl.startsWith("https://"));

        if (!shopUrl.isEmpty() && !(shopUrl.startsWith("http://") || shopUrl.startsWith("https://"))) {
            source.sendFailure(Component.translatable("commands.shoplinker.add.invalid_url"));
            return 0;
        }

        UUID playerUUID = source.getPlayerOrException().getUUID();

        ShopEntry newShop = new ShopEntry(shopName, shopDescription, shopUrl, playerUUID);
        if (ShopLinker.shopManager.addShop(newShop)) {
            source.sendSuccess(() -> Component.translatable("commands.shoplinker.add.success", shopName), true);
        } else {
            source.sendFailure(Component.translatable("commands.shoplinker.add.exists", shopName));
        }
        return 1;
    }

    private static int removeShop(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String shopName = StringArgumentType.getString(context, "name");

        Optional<ShopEntry> shopToRemove = ShopLinker.shopManager.getShopByName(shopName);

        if (shopToRemove.isPresent()) {
            ShopEntry shop = shopToRemove.get();
            if (source.hasPermission(2) || source.getPlayerOrException().getUUID().equals(shop.getOwnerUUID())) {
                if (ShopLinker.shopManager.removeShop(shopName)) {
                    source.sendSuccess(() -> Component.translatable("commands.shoplinker.remove.success", shopName), true);
                } else {
                    source.sendFailure(Component.translatable("commands.shoplinker.remove.not_found", shopName));
                }
            } else {
                source.sendFailure(Component.translatable("commands.shoplinker.remove.permission"));
            }
        } else {
            source.sendFailure(Component.translatable("commands.shoplinker.remove.not_found", shopName));
        }
        return 1;
    }

    private static int modifyShop(CommandContext<CommandSourceStack> context, String fieldToModify) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String oldShopName = StringArgumentType.getString(context, "shopName");
        
        Optional<ShopEntry> optionalShop = ShopLinker.shopManager.getShopByName(oldShopName);

        if (optionalShop.isEmpty()) {
            source.sendFailure(Component.translatable("commands.shoplinker.modify.not_found", oldShopName));
            return 0;
        }

        ShopEntry shopToModify = optionalShop.get();
        UUID playerUUID = source.getPlayerOrException().getUUID();

        if (!source.hasPermission(2) && !playerUUID.equals(shopToModify.getOwnerUUID())) {
            source.sendFailure(Component.translatable("commands.shoplinker.generic.no_permission"));
            return 0;
        }

        boolean modified = false;
        String tempNewValue = ""; 

        switch (fieldToModify) {
            case "name":
                tempNewValue = StringArgumentType.getString(context, "newName");
                if (ShopLinker.shopManager.getShopByName(tempNewValue).isPresent() && !tempNewValue.equalsIgnoreCase(oldShopName)) {
                    source.sendFailure(Component.translatable("commands.shoplinker.modify.name_exists", tempNewValue));
                    return 0;
                }
                shopToModify.setName(tempNewValue);
                modified = true;
                break;
            case "description":
                tempNewValue = StringArgumentType.getString(context, "newDescription");
                shopToModify.setDescription(tempNewValue);
                modified = true;
                break;
            case "url":
                tempNewValue = StringArgumentType.getString(context, "newUrl");
                if (!tempNewValue.isEmpty() && !(tempNewValue.startsWith("http://") || tempNewValue.startsWith("https://"))) {
                    source.sendFailure(Component.translatable("commands.shoplinker.add.invalid_url"));
                    return 0;
                }
                shopToModify.setUrl(tempNewValue);
                modified = true;
                break;
        }

        if (modified) {
            final String finalNewValue = tempNewValue;
            // Utilisation de fieldToModify directement, comme vous l'avez demandé, sans clés de traduction pour les noms de champs.
            source.sendSuccess(() -> Component.translatable("commands.shoplinker.modify.success", oldShopName, fieldToModify, finalNewValue), true);
            ShopLinker.shopManager.updateShop(shopToModify); // Déclenche la sauvegarde via ShopManager
        } else {
            source.sendFailure(Component.translatable("commands.shoplinker.generic.error"));
        }

        return 1;
    }
}