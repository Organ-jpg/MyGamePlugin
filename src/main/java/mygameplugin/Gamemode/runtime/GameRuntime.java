package mygameplugin.gamemode.runtime;

import java.util.UUID;
import mygameplugin.gamemode.GameContext;
import mygameplugin.gamemode.GameSession;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public interface GameRuntime {
    default void onPreparing(GameContext context) {
    }

    default void onStart(GameContext context) {
    }

    default void onEnd(GameSession session) {
    }

    default void onPlayerLeave(GameSession session, UUID playerId) {
    }

    default void onPlayerRejoin(GameSession session, Player player) {
    }

    default boolean managesSpawns() {
        return false;
    }

    default void onBlockBreak(GameSession session, BlockBreakEvent event) {
    }

    default void onBlockPlace(GameSession session, BlockPlaceEvent event) {
    }

    default void onPlayerDeath(GameSession session, PlayerDeathEvent event) {
    }

    default void onPlayerRespawn(GameSession session, PlayerRespawnEvent event) {
    }
}
