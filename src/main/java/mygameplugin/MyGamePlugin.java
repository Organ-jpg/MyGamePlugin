package mygameplugin;

import mygameplugin.command.LeaveCommand;
import mygameplugin.command.PlayGameCommand;
import mygameplugin.gui.GameGUI;
import mygameplugin.listener.BlockListener;
import mygameplugin.listener.DamageListener;
import mygameplugin.listener.GamePlayerListener;
import mygameplugin.listener.InventoryListener;
import mygameplugin.listener.MenuListener;
import mygameplugin.listener.PlayerListener;
import mygameplugin.manager.GameManager;
import mygameplugin.manager.LobbyManager;
import mygameplugin.world.FixedMapWorldService;
import mygameplugin.world.TemporaryWorldService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MyGamePlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        TemporaryWorldService temporaryWorldService = new TemporaryWorldService(this);
        FixedMapWorldService fixedMapWorldService = new FixedMapWorldService();
        GameManager gameManager = new GameManager(this, temporaryWorldService, fixedMapWorldService);
        LobbyManager lobbyManager = new LobbyManager(gameManager);
        GameGUI gameGUI = new GameGUI(gameManager, lobbyManager);

        registerCommand("playgame", new PlayGameCommand(gameManager, lobbyManager, gameGUI));
        registerCommand("leave", new LeaveCommand(gameManager, lobbyManager));

        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new MenuListener(gameGUI), this);
        pluginManager.registerEvents(new PlayerListener(gameManager), this);
        pluginManager.registerEvents(new InventoryListener(gameManager), this);
        pluginManager.registerEvents(new DamageListener(gameManager), this);
        pluginManager.registerEvents(new BlockListener(gameManager), this);
        pluginManager.registerEvents(new GamePlayerListener(gameManager), this);
    }

    private void registerCommand(String label, Object executor) {
        PluginCommand command = getCommand(label);
        if (command == null) {
            getLogger().warning("Command not found in plugin.yml: " + label);
            return;
        }
        if (executor instanceof PlayGameCommand playGameCommand) {
            command.setExecutor(playGameCommand);
            command.setTabCompleter(playGameCommand);
            return;
        }
        if (executor instanceof LeaveCommand leaveCommand) {
            command.setExecutor(leaveCommand);
        }
    }
}
