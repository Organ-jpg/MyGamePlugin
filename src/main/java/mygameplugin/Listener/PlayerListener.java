package mygameplugin.listener;

import java.util.Locale;
import java.util.Set;
import mygameplugin.manager.GameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private static final Set<String> ALLOWED = Set.of("playgame", "leave");
    private final GameManager gameManager;

    public PlayerListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!gameManager.isCommandRestricted(event.getPlayer())) {
            return;
        }
        String raw = event.getMessage().startsWith("/") ? event.getMessage().substring(1) : event.getMessage();
        String label = raw.split(" ")[0].toLowerCase(Locale.ROOT);
        int idx = label.indexOf(':');
        if (idx >= 0 && idx < label.length() - 1) label = label.substring(idx + 1);
        if (!ALLOWED.contains(label)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("Only /playgame and /leave are allowed.");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        gameManager.handleReconnect(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        gameManager.handleDisconnect(event.getPlayer());
    }
}
