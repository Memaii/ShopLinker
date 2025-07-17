# 🛍️ ShopLinker - Minecraft Mod

**ShopLinker** is a NeoForge mod for Minecraft that allows players to create, manage, and display virtual shops directly within their server. With a simple command-line interface, players can effortlessly share creations, services, or external resources.

---

## ✨ Features

- **Create Shops**  
  Add new shops with a unique name, description, and an optional URL (e.g., external store, Discord, image, etc.).

- **Manage Shops**  
  Modify or remove existing shops. Shop owners and server operators have full control over shop management.

- **List Shops**  
  View a list of all registered shops, including their descriptions and clickable links (if provided).

- **Shop Info**  
  Retrieve detailed information about any shop: name, description, URL, and owner's UUID.

- **Persistent Data**  
  All shop data is saved in a JSON file, ensuring data persists across server restarts.

---

## 🛠️ Installation

### Requirements

- Minecraft with a compatible version of NeoForge installed.

### Steps

1. **Download NeoForge**  
   Ensure you have the correct version of NeoForge installed on both your client and server.

2. **Download ShopLinker**  
   Get the latest `.jar` file from the [releases page](https://github.com/YourGitHubUser/ShopLinker/releases) *(or build it yourself – see below)*.

3. **Install on Client**  
   Place the `.jar` file in your `mods/` folder of your Minecraft client.

4. **Install on Server**  
   Place the `.jar` file in the server's `mods/` folder.

5. **Run Minecraft / Start Server**

---

## 💬 Commands

The base command is `/shop`, available to all players.

| Subcommand                                      | Description                                 | Permission                  |
|-------------------------------------------------|---------------------------------------------|-----------------------------|
| `/shop add <name> <description> [url]`          | Adds a new shop.                            | Level 2 (Operator/Owner)    |
| `/shop remove <name>`                           | Removes an existing shop.                   | Level 2 or shop owner       |
| `/shop modify <name> <field> <newValue>`        | Modifies shop details (`description`, `url`, `name`). | Level 2 or shop owner       |
| `/shop list`                                    | Lists all registered shops.                 | Level 0 (Everyone)          |
| `/shop info <name>`                             | Displays details about a specific shop.     | Level 0 (Everyone)          |

---

## 🧾 Examples

```bash
/shop add MyAwesomeShop "Selling rare diamonds!"
/shop add MyWebStore "Check out my full catalog!" https://mywebstore.com

/shop remove MyAwesomeShop

/shop modify MyAwesomeShop description "Now selling enchanted gear!"
/shop modify MyAwesomeShop url "https://newlink.com"
/shop modify OldShop name NewShop

/shop list

/shop info MyWebStore
```

---

## 💾 Data Storage

Shop data is saved to a file named `shoplinker_shops.json`, located in your world’s root directory (same level as `playerdata`, `data`, etc.). This ensures each world has its own persistent shop database.

---

## 🧪 Building from Source

If you want to build ShopLinker manually:

```bash
git clone https://github.com/YourGitHubUser/ShopLinker.git
cd ShopLinker
./gradlew build
```
On Windows, use `gradlew build` instead.

The final `.jar` will be located in `build/libs/`.

---

## 🤝 Contributing

Contributions are welcome!  
Feel free to open an issue or submit a pull request if you have bug reports, suggestions, or new features in mind.

---

## 📄 License

This project is licensed under the MIT License.