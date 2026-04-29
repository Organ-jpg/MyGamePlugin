package mygameplugin.feature.items;

import java.util.List;
import mygameplugin.feature.specialItem.SpecialItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CompassItem implements SpecialItem {
    @Override
    public String id() {
        return "compass";
    }

    @Override
    public void use(Player player) {
        ItemStack compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Tracker Compass");
            meta.setLore(List.of("Tracks the current ring center"));
            compass.setItemMeta(meta);
        }
        player.getInventory().setItem(0, compass);
    }
}
