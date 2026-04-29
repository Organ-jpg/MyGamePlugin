package mygameplugin.world;

import mygameplugin.gamemode.GameModeType;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class FixedMapWorldService implements WorldService {
    @Override
    public World prepareWorld(GameModeType modeType) {
        World exact = Bukkit.getWorld(modeType.name().toLowerCase());
        if (exact != null) {
            return exact;
        }
        World namedArena = Bukkit.getWorld("arena");
        if (namedArena != null) {
            return namedArena;
        }
        return Bukkit.getWorlds().getFirst();
    }
}
