package mygameplugin.gamemode.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import mygameplugin.MyGamePlugin;
import mygameplugin.border.BorderManager;
import mygameplugin.feature.items.CompassItem;
import mygameplugin.gamemode.GameContext;
import mygameplugin.gamemode.GameSession;
import mygameplugin.manager.GameManager;
import mygameplugin.service.ScoreboardService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scheduler.BukkitTask;

public class SurvivalPvPRuntime implements GameRuntime {
    private static final long PHASE_DELAY_TICKS = 15L * 60L * 20L;

    private final MyGamePlugin plugin;
    private final GameManager gameManager;
    private final BorderManager borderManager = new BorderManager();
    private final ScoreboardService scoreboardService = new ScoreboardService();
    private final Random random = new Random();
    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Set<UUID> spectators = new HashSet<>();
    private final Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private BukkitTask scoreboardTask;
    private BukkitTask compassTask;
    private BukkitTask phaseTask;
    private Location ringCenter;
    private int phase;
    private long nextPhaseTick;
    private GameSession session;

    public SurvivalPvPRuntime(MyGamePlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public boolean managesSpawns() {
        return true;
    }

    @Override
    public void onPreparing(GameContext context) {
        this.session = context.session();
        alivePlayers.clear();
        spectators.clear();
        scoreboards.clear();
        phase = 0;
        ringCenter = randomCenter(context.world(), context.world().getSpawnLocation(), 180.0);
    }

    @Override
    public void onStart(GameContext context) {
        World world = context.world();
        WorldBorder border = world.getWorldBorder();
        borderManager.configure(world, ringCenter, 500.0);
        border.setDamageBuffer(0.0);
        border.setDamageAmount(1.0);
        border.setWarningDistance(12);

        List<Location> spawns = generateSpawnLocations(world, context.players().size());
        int index = 0;
        for (UUID playerId : context.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            player.teleport(spawns.get(Math.min(index, spawns.size() - 1)));
            player.setGameMode(GameMode.SURVIVAL);
            new CompassItem().use(player);
            player.setCompassTarget(ringCenter);
            alivePlayers.add(playerId);
            scoreboardService.create(player, "survival_" + player.getName(), ChatColor.RED + "Survival PvP");
            scoreboards.put(playerId, player.getScoreboard());
            index++;
        }

        nextPhaseTick = world.getFullTime() + PHASE_DELAY_TICKS;
        startScoreboardTask(world);
        startCompassTask();
        scheduleNextPhase(world);
    }

    @Override
    public void onEnd(GameSession session) {
        if (scoreboardTask != null) {
            scoreboardTask.cancel();
        }
        if (compassTask != null) {
            compassTask.cancel();
        }
        if (phaseTask != null) {
            phaseTask.cancel();
        }
    }

    @Override
    public void onBlockBreak(GameSession session, BlockBreakEvent event) {
    }

    @Override
    public void onBlockPlace(GameSession session, BlockPlaceEvent event) {
    }

    @Override
    public void onPlayerDeath(GameSession session, PlayerDeathEvent event) {
        UUID playerId = event.getEntity().getUniqueId();
        if (!session.players().contains(playerId)) {
            return;
        }
        event.getDrops().clear();
        event.setKeepLevel(true);
        alivePlayers.remove(playerId);
        spectators.add(playerId);
        Bukkit.getScheduler().runTaskLater(plugin, this::checkVictory, 2L);
    }

    @Override
    public void onPlayerRespawn(GameSession session, PlayerRespawnEvent event) {
        if (!spectators.contains(event.getPlayer().getUniqueId())) {
            return;
        }
        event.setRespawnLocation(ringCenter);
        Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().setGameMode(GameMode.SPECTATOR));
    }

    @Override
    public void onPlayerLeave(GameSession session, UUID playerId) {
        alivePlayers.remove(playerId);
        spectators.remove(playerId);
        checkVictory();
    }

    private void startScoreboardTask(World world) {
        scoreboardTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID playerId : session.players()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null) {
                    continue;
                }
                updateBoard(player, world);
            }
        }, 20L, 20L);
    }

    private void startCompassTask() {
        compassTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID playerId : session.players()) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.setCompassTarget(ringCenter);
                }
            }
        }, 20L, 40L);
    }

    private void scheduleNextPhase(World world) {
        phaseTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            advancePhase(world);
            if (phase < 3) {
                nextPhaseTick = world.getFullTime() + PHASE_DELAY_TICKS;
                scheduleNextPhase(world);
            }
        }, PHASE_DELAY_TICKS);
    }

    private void advancePhase(World world) {
        phase++;
        WorldBorder border = world.getWorldBorder();
        ringCenter = randomCenter(world, ringCenter, Math.max(40.0, border.getSize() / 4.0));
        border.setCenter(ringCenter);
        double targetSize = switch (phase) {
            case 1 -> 333.0;
            case 2 -> 166.0;
            default -> 1.0;
        };
        long shrinkSeconds = phase == 3 ? 900L : 300L;
        border.setDamageAmount(phase + 1.0);
        borderManager.shrink(world, targetSize, shrinkSeconds);
        Bukkit.broadcastMessage("Survival PvP phase " + phase + " has started. The border is shrinking.");
    }

    private void updateBoard(Player player, World world) {
        Scoreboard board = scoreboards.get(player.getUniqueId());
        if (board == null) {
            return;
        }
        Objective objective = board.getObjective(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
        if (objective == null) {
            return;
        }
        for (String entry : new ArrayList<>(board.getEntries())) {
            board.resetScores(entry);
        }
        boolean outside = isOutsideBorder(player.getLocation(), world.getWorldBorder());
        int aliveCount = alivePlayers.size();
        int spectatorCount = spectators.size();
        long ticksRemaining = Math.max(0L, nextPhaseTick - world.getFullTime());
        List<String> lines = List.of(
                ChatColor.WHITE + "Mode: Survival PvP",
                ChatColor.GRAY + "Phase: " + phase + "/3",
                ChatColor.RED + "Border: " + (int) Math.round(world.getWorldBorder().getSize()),
                ChatColor.YELLOW + "Ring In: " + formatTicks(ticksRemaining),
                outside ? ChatColor.DARK_RED + "Outside safe zone" : ChatColor.GREEN + "Inside safe zone",
                ChatColor.AQUA + "Alive: " + aliveCount,
                ChatColor.BLUE + "Spectators: " + spectatorCount);

        int score = lines.size();
        for (String line : lines) {
            objective.getScore(line).setScore(score--);
        }
    }

    private List<Location> generateSpawnLocations(World world, int count) {
        List<Location> spawns = new ArrayList<>();
        int attempts = 0;
        while (spawns.size() < count && attempts < count * 40) {
            attempts++;
            double offsetX = (random.nextDouble() - 0.5) * 360.0;
            double offsetZ = (random.nextDouble() - 0.5) * 360.0;
            int x = ringCenter.getBlockX() + (int) Math.round(offsetX);
            int z = ringCenter.getBlockZ() + (int) Math.round(offsetZ);
            int y = world.getHighestBlockYAt(x, z) + 1;
            Location location = new Location(world, x + 0.5, y, z + 0.5);
            if (isFarEnough(location, spawns, 45.0)) {
                spawns.add(location);
            }
        }
        while (spawns.size() < count) {
            spawns.add(world.getSpawnLocation().clone().add(0.5, 1.0, 0.5));
        }
        return spawns;
    }

    private boolean isFarEnough(Location candidate, List<Location> existing, double minDistance) {
        for (Location location : existing) {
            if (location.distance(candidate) < minDistance) {
                return false;
            }
        }
        return true;
    }

    private Location randomCenter(World world, Location around, double radius) {
        double x = around.getX() + ((random.nextDouble() * 2.0) - 1.0) * radius;
        double z = around.getZ() + ((random.nextDouble() * 2.0) - 1.0) * radius;
        int y = world.getHighestBlockYAt((int) Math.round(x), (int) Math.round(z)) + 1;
        return new Location(world, x, y, z);
    }

    private boolean isOutsideBorder(Location location, WorldBorder border) {
        double half = border.getSize() / 2.0;
        return Math.abs(location.getX() - border.getCenter().getX()) > half
                || Math.abs(location.getZ() - border.getCenter().getZ()) > half;
    }

    private String formatTicks(long ticks) {
        long totalSeconds = ticks / 20L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void checkVictory() {
        Set<String> survivors = new LinkedHashSet<>();
        for (UUID playerId : alivePlayers) {
            String team = session.teamSelections().get(playerId);
            survivors.add(team == null ? playerId.toString() : team);
        }
        if (survivors.size() == 1 && !survivors.isEmpty()) {
            gameManager.endGame(survivors.iterator().next() + " wins Survival PvP.");
        }
    }
}
