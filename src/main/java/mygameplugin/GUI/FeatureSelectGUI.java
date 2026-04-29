package mygameplugin.gui;

import java.util.List;
import mygameplugin.gamemode.FeatureFlag;
import mygameplugin.gamemode.GameSession;
import mygameplugin.gamemode.OptionState;
import mygameplugin.gamemode.TeamAssignMode;
import mygameplugin.gamemode.WorldMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class FeatureSelectGUI {
    public static final String TITLE = "Configure Match";

    public void open(Player player, GameSession session) {
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE);
        ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            boolean reserved = slot == 4 || slot == 10 || slot == 11 || slot == 12 || slot == 19 || slot == 20
                    || slot == 28 || slot == 29 || slot == 45 || slot == 53;
            if (!reserved) {
                inventory.setItem(slot, filler);
            }
        }

        inventory.setItem(4, named(session.mode().icon(), session.mode().displayName()));
        placeFeature(inventory, 10, Material.BARRIER, FeatureFlag.BORDER, session);
        placeFeature(inventory, 11, Material.BLAZE_POWDER, FeatureFlag.ABILITY, session);
        placeFeature(inventory, 12, Material.CHEST, FeatureFlag.SPECIAL_ITEMS, session);
        placeWorldMode(inventory, 19, WorldMode.TEMPORARY, session);
        placeWorldMode(inventory, 20, WorldMode.FIXED_MAP, session);
        placeTeamMode(inventory, 28, TeamAssignMode.ASSIGN, session);
        placeTeamMode(inventory, 29, TeamAssignMode.RANDOM, session);
        inventory.setItem(45, named(Material.ARROW, "Back"));
        inventory.setItem(53, named(Material.LIME_CONCRETE, "Create Lobby"));
        player.openInventory(inventory);
    }

    private void placeFeature(Inventory inventory, int slot, Material material, FeatureFlag featureFlag, GameSession session) {
        OptionState rule = session.mode().featureRule(featureFlag);
        boolean enabled = session.hasFeature(featureFlag);
        inventory.setItem(slot, described(material, rule.symbol() + " " + featureFlag.displayName(), List.of(
                rule.description(),
                enabled ? "Enabled" : "Disabled",
                rule.locked() ? "This setting is locked" : "Click to toggle")));
    }

    private void placeWorldMode(Inventory inventory, int slot, WorldMode worldMode, GameSession session) {
        OptionState rule = session.mode().worldRule(worldMode);
        boolean selected = session.worldMode() == worldMode;
        Material material = worldMode == WorldMode.TEMPORARY ? Material.GRASS_BLOCK : Material.MAP;
        inventory.setItem(slot, described(material, rule.symbol() + " " + worldMode.displayName(), List.of(
                rule.description(),
                selected ? "Selected" : "Not selected",
                rule.locked() ? "This setting is locked" : "Click to select")));
    }

    private void placeTeamMode(Inventory inventory, int slot, TeamAssignMode teamAssignMode, GameSession session) {
        OptionState rule = session.mode().teamRule(teamAssignMode);
        boolean selected = session.teamAssignMode() == teamAssignMode;
        Material material = teamAssignMode == TeamAssignMode.ASSIGN ? Material.LIME_DYE : Material.GRAY_DYE;
        inventory.setItem(slot, described(material, rule.symbol() + " " + teamAssignMode.displayName(), List.of(
                rule.description(),
                selected ? "Selected" : "Not selected",
                "Same color means same team")));
    }

    private ItemStack named(Material material, String name) {
        return described(material, name, List.of());
    }

    private ItemStack described(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
