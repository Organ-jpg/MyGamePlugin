package mygameplugin.gamemode.runtime;

import java.util.Map;
import java.util.UUID;
import mygameplugin.MyGamePlugin;
import mygameplugin.feature.items.CompassItem;
import mygameplugin.gamemode.GameContext;
import mygameplugin.gamemode.GameSession;
import mygameplugin.gamemode.imposter.ImposterRole;
import mygameplugin.gamemode.imposter.ImposterRoleService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ImposterRuntime implements GameRuntime {
    private final MyGamePlugin plugin;
    private final ImposterRoleService roleService = new ImposterRoleService();

    public ImposterRuntime(MyGamePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPreparing(GameContext context) {
        Map<UUID, ImposterRole> roles = roleService.assignRoles(context.players());
        for (UUID playerId : context.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            ImposterRole role = roles.get(playerId);
            player.sendTitle(role.title(), role.subtitle(), 10, 70, 20);
            player.sendMessage("Objective: Innocents kill the Ender Dragon. Imposters survive or eliminate players.");
            new CompassItem().use(player);
            player.setCompassTarget(context.world().getSpawnLocation());
        }
    }

    @Override
    public void onStart(GameContext context) {
        int minutes = context.players().size() <= 6 ? 50 : 40;
        for (UUID playerId : context.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("Imposter timer started: " + minutes + " minutes.");
            }
        }
    }

    @Override
    public void onEnd(GameSession session) {
        plugin.getLogger().info("Imposter match ended.");
    }
}
