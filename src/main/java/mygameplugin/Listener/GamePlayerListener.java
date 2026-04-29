package mygameplugin.listener;

import mygameplugin.manager.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class GamePlayerListener implements Listener {
    private final GameManager gameManager;

    public GamePlayerListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        gameManager.handlePlayerDeath(event);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        gameManager.handlePlayerRespawn(event);
    }
}
