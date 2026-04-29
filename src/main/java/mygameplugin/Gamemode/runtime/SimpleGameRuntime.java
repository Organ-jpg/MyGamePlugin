package mygameplugin.gamemode.runtime;

import mygameplugin.gamemode.GameContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SimpleGameRuntime implements GameRuntime {
    private final String displayName;

    public SimpleGameRuntime(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public void onStart(GameContext context) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (context.players().contains(player.getUniqueId())) {
                player.sendMessage(displayName + " systems initialized.");
            }
        }
    }
}
