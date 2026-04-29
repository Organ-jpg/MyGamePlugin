package mygameplugin.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreboardService {
    public Scoreboard create(Player player, String objectiveName, String title) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        if (manager == null) {
            return null;
        }
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective(objectiveName, "dummy", title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(board);
        return board;
    }
}
