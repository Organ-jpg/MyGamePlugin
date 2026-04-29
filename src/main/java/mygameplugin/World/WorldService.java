package mygameplugin.world;

import mygameplugin.gamemode.GameModeType;
import org.bukkit.World;

public interface WorldService {
    World prepareWorld(GameModeType modeType);
}
