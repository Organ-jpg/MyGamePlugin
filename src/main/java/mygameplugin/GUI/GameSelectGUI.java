package mygameplugin.gui;

import java.util.List;
import mygameplugin.gamemode.GameModeType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GameSelectGUI {
    public static final String TITLE = "Choose Game Type";

    public void open(Player player, GameModeType selectedMode) {
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);
        fillFrame(inventory);
        placeMode(inventory, 20, GameModeType.BEACON_DEFENCE_WAR, selectedMode,
                List.of("Team versus team beacon warfare", "Each team owns a beacon", "Destroy enemy beacons to win"));
        placeMode(inventory, 24, GameModeType.SURVIVAL_PVP, selectedMode,
                List.of("Battle royale survival match", "Border phases and random shrink centers", "Last player or last team wins"));
        inventory.setItem(49, named(Material.BARRIER, "Back"));
        player.openInventory(inventory);
    }

    private void placeMode(Inventory inventory, int slot, GameModeType modeType, GameModeType selectedMode, List<String> lore) {
        ItemStack item = new ItemStack(modeType.icon());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName((modeType == selectedMode ? "[Selected] " : "") + modeType.displayName());
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inventory.setItem(slot, item);
    }

    private void fillFrame(Inventory inventory) {
        ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot != 20 && slot != 24 && slot != 49) {
                inventory.setItem(slot, filler);
            }
        }
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
