package mygameplugin.gui;

import java.util.Set;
import java.util.UUID;
import mygameplugin.gamemode.TeamAssignMode;
import mygameplugin.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LobbyGUI {
    public static final String TITLE = "Lobby";

    public void open(Player viewer, GameManager gameManager) {
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);
        ItemStack filler = named(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler);
        }

        Set<UUID> players = gameManager.getLobbyPlayers();
        int[] playerSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        int index = 0;
        for (UUID uuid : players) {
            if (index >= playerSlots.length) {
                break;
            }
            OfflinePlayer target = Bukkit.getOfflinePlayer(uuid);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = head.getItemMeta();
            if (meta != null) {
                String name = target.getName() == null ? uuid.toString() : target.getName();
                String teamColor = gameManager.teamColorOf(uuid);
                meta.setDisplayName(name + (uuid.equals(gameManager.getHost()) ? " (Host)" : ""));
                meta.setLore(teamColor == null ? java.util.List.of("No team selected") : java.util.List.of("Team: " + teamColor));
                head.setItemMeta(meta);
            }
            inventory.setItem(playerSlots[index++], head);
        }

        inventory.setItem(45, named(Material.LIME_CONCRETE, "Start Game"));
        inventory.setItem(47, named(gameManager.getTeamAssignMode() == TeamAssignMode.ASSIGN ? Material.LIME_DYE : Material.GRAY_DYE,
                gameManager.getTeamAssignMode() == TeamAssignMode.ASSIGN ? "Assign Team" : "Random Team"));
        inventory.setItem(49, named(Material.BARRIER, "Leave Lobby"));
        if (gameManager.getTeamAssignMode() == TeamAssignMode.ASSIGN) {
            inventory.setItem(50, named(Material.RED_WOOL, "Red Team"));
            inventory.setItem(51, named(Material.BLUE_WOOL, "Blue Team"));
            inventory.setItem(52, named(Material.GREEN_WOOL, "Green Team"));
            inventory.setItem(53, named(Material.YELLOW_WOOL, "Yellow Team"));
        }

        viewer.openInventory(inventory);
    }

    private ItemStack named(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }
}
