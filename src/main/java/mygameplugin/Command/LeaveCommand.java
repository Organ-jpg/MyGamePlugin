package mygameplugin.command;

import mygameplugin.gamemode.GameState;
import mygameplugin.manager.GameManager;
import mygameplugin.manager.LobbyManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {
    private final GameManager gameManager;
    private final LobbyManager lobbyManager;

    public LeaveCommand(GameManager gameManager, LobbyManager lobbyManager) {
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (gameManager.getState() == GameState.LOBBY) {
            boolean removed = lobbyManager.leaveLobby(player);
            player.sendMessage(removed ? "You left the lobby." : "You are not in the lobby.");
            return true;
        }

        if (gameManager.getState() == GameState.INGAME) {
            boolean removed = gameManager.leaveFromGame(player);
            player.sendMessage(removed ? "You left the game." : "You are not in the current game.");
            return true;
        }

        player.sendMessage("There is no lobby or game to leave right now.");
        return true;
    }
}
