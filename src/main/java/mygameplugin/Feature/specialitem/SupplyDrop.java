package mygameplugin.feature.items;

import mygameplugin.feature.specialItem.SpecialItem;
import org.bukkit.entity.Player;

public class SupplyDrop implements SpecialItem {
    @Override
    public String id() {
        return "supply_drop";
    }

    @Override
    public void use(Player player) {
        player.sendMessage("Supply drop requested.");
    }
}
