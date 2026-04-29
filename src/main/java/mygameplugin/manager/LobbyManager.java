package mygameplugin.manager;

import org.bukkit.entity.Player;

public class LobbyManager {
    private final GameManager gameManager;

    public LobbyManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public boolean leaveLobby(Player player) {
        if (!gameManager.hasSession() || !gameManager.getLobbyPlayers().contains(player.getUniqueId())) {
            return false;
        }
        return gameManager.leaveFromGame(player);
    }

    public boolean joinLobby(Player player) {
        return gameManager.joinLobby(player);
    }
}
