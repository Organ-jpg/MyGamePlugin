package mygameplugin.listener;

import mygameplugin.manager.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public class InventoryListener implements Listener {
    private final GameManager gameManager;

    public InventoryListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (gameManager.shouldLockInventory()) event.setCancelled(true);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (gameManager.shouldLockInventory()) event.setCancelled(true);
    }
}
