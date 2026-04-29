package mygameplugin.listener;

import mygameplugin.gui.GameGUI;
import mygameplugin.gui.GameSelectGUI;
import mygameplugin.gui.FeatureSelectGUI;
import mygameplugin.gui.LobbyGUI;
import mygameplugin.gui.MainMenuGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuListener implements Listener {
    private final GameGUI gameGUI;

    public MenuListener(GameGUI gameGUI) {
        this.gameGUI = gameGUI;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();
        if (!MainMenuGUI.TITLE.equals(title)
                && !GameSelectGUI.TITLE.equals(title)
                && !FeatureSelectGUI.TITLE.equals(title)
                && !LobbyGUI.TITLE.equals(title)) {
            return;
        }
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }
        gameGUI.handleClick(player, event.getView(), event.getSlot(), event.getCurrentItem().getType());
    }
}
