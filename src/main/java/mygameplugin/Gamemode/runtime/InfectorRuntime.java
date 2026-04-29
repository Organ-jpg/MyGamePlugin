package mygameplugin.gamemode.runtime;

import java.util.Map;
import java.util.UUID;
import mygameplugin.MyGamePlugin;
import mygameplugin.feature.items.CompassItem;
import mygameplugin.gamemode.GameContext;
import mygameplugin.gamemode.GameSession;
import mygameplugin.gamemode.infector.InfectorRole;
import mygameplugin.gamemode.infector.InfectorRoleService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InfectorRuntime implements GameRuntime {
    private final MyGamePlugin plugin;
    private final InfectorRoleService roleService = new InfectorRoleService();

    public InfectorRuntime(MyGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPreparing(GameContext context) {
        Map<UUID, InfectorRole> roles = roleService.assignRoles(context.players());
        for (UUID playerId : context.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            InfectorRole role = roles.get(playerId);
            player.sendTitle(role.title(), role.subtitle(), 10, 70, 20);
            if (role == InfectorRole.QUEEN) {
                player.sendMessage("Infected Queen has been chosen!");
            }
            new CompassItem().use(player);
            player.setCompassTarget(context.world().getSpawnLocation());
        }
    }

    @Override
    public void onStart(GameContext context) {
        for (UUID playerId : context.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("Infection system enabled. Runners must defeat the Ender Dragon.");
            }
        }
    }

    @Override
    public void onEnd(GameSession session) {
        plugin.getLogger().info("Infector match ended.");
    }
}
