package mygameplugin.listener;

import mygameplugin.gamemode.GameState;
import mygameplugin.manager.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class BlockListener implements Listener {
    private final GameManager gameManager;

    public BlockListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onBed(PlayerBedEnterEvent event) {
        GameState state = gameManager.getState();
        if (state == GameState.STARTING || state == GameState.PREPARING || state == GameState.INGAME) event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (gameManager.getState() == GameState.STARTING) {
            event.setCancelled(true);
            return;
        }
        gameManager.handleBlockBreak(event);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (gameManager.getState() == GameState.STARTING) {
            event.setCancelled(true);
            return;
        }
        gameManager.handleBlockPlace(event);
    }
}
