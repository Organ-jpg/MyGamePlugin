package mygameplugin.gui;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MainMenuGUI {
    public static final String TITLE = "Play Game";

    public void open(Player player, boolean hasSession, boolean joinable) {
        Inventory inventory = Bukkit.createInventory(null, 27, TITLE);
        ItemStack filler = button(Material.BLACK_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }

        inventory.setItem(11, button(Material.EMERALD, "Create Game",
                List.of("Host a new match", hasSession ? "Status: session already exists" : "Status: ready to create")));
        inventory.setItem(13, button(Material.BEACON, "Supported Modes",
                List.of("Beacon Defence War", "Survival PvP")));
        inventory.setItem(15, button(Material.PLAYER_HEAD, "Join Game",
                List.of("Join the active lobby", joinable ? "Status: lobby available" : "Status: no lobby yet")));
        player.openInventory(inventory);
    }

    private ItemStack button(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }
        return item;
    }
}
