package mygameplugin.command;

import mygameplugin.gui.GameGUI;
import mygameplugin.manager.GameManager;
import mygameplugin.manager.LobbyManager;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class PlayGameCommand implements CommandExecutor, TabCompleter {
    private final GameManager gameManager;
    private final LobbyManager lobbyManager;
    private final GameGUI gameGUI;

    public PlayGameCommand(GameManager gameManager, LobbyManager lobbyManager, GameGUI gameGUI) {
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
        this.gameGUI = gameGUI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        // Open main game menu for create/join actions.
        gameGUI.openMain(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }
}
