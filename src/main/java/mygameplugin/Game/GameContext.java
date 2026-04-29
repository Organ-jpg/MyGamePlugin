package mygameplugin.gamemode;

import java.util.Set;
import java.util.UUID;
import mygameplugin.MyGamePlugin;
import org.bukkit.World;

public record GameContext(MyGamePlugin plugin, GameModeType gameModeType, World world, Set<UUID> players, GameSession session) {
}
