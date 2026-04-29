package mygameplugin.feature.ability;

import org.bukkit.entity.Player;

public interface Ability {
    String id();

    void activate(Player player);
}
