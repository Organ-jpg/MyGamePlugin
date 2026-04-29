package mygameplugin.feature.specialItem;

import org.bukkit.entity.Player;

public interface SpecialItem {
    String id();

    void use(Player player);
}
