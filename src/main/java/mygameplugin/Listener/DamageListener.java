package mygameplugin.listener;

import mygameplugin.gamemode.GameState;
import mygameplugin.manager.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageListener implements Listener {
    private final GameManager gameManager;

    public DamageListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onGenericDamage(EntityDamageEvent event) {
        if (gameManager.getState() == GameState.PREPARING || gameManager.getState() == GameState.STARTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPvp(EntityDamageByEntityEvent event) {
        if (gameManager.getState() == GameState.PREPARING || gameManager.getState() == GameState.STARTING) {
            event.setCancelled(true);
        }
    }
}
