package mygameplugin.gamemode.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import mygameplugin.MyGamePlugin;
import mygameplugin.border.BorderManager;
import mygameplugin.feature.items.CompassItem;
import mygameplugin.gamemode.GameContext;
import mygameplugin.gamemode.GameSession;
import mygameplugin.manager.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

public class BeaconDefenceWarRuntime implements GameRuntime {
    private final MyGamePlugin plugin;
    private final GameManager gameManager;
    private final BorderManager borderManager = new BorderManager();
    private final Map<String, BeaconTeam> teams = new LinkedHashMap<>();
    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Set<UUID> respawningPlayers = new HashSet<>();
    private final Map<UUID, Location> pendingRespawns = new HashMap<>();
    private BukkitTask beamTask;
    private BukkitTask resistanceTask;
    private boolean firstBeaconDestroyed;
    private Location ringCenter;

    public BeaconDefenceWarRuntime(MyGamePlugin plugin, GameManager gameManager) {
        this.plugin = plugin;
        this.gameManager = gameManager;
    }

    @Override
    public boolean managesSpawns() {
        return true;
    }

    @Override
    public void onPreparing(GameContext context) {
        teams.clear();
        playerTeams.clear();
        alivePlayers.clear();
        respawningPlayers.clear();
        pendingRespawns.clear();
        firstBeaconDestroyed = false;

        World world = context.world();
        Map<String, Set<UUID>> grouped = buildTeams(context.session());
        List<String> orderedTeams = new ArrayList<>(grouped.keySet());
        Location center = world.getSpawnLocation().clone();
        ringCenter = center.clone();
        double radius = Math.max(70.0, 35.0 * orderedTeams.size());

        for (int index = 0; index < orderedTeams.size(); index++) {
            String teamName = orderedTeams.get(index);
            double angle = (Math.PI * 2.0 * index) / orderedTeams.size();
            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * radius);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * radius);
            Location beaconLocation = prepareBeaconLocation(world, x, z);
            teams.put(teamName, new BeaconTeam(teamName, beaconLocation, grouped.get(teamName)));
            for (UUID member : grouped.get(teamName)) {
                playerTeams.put(member, teamName);
            }
        }
    }

    @Override
    public void onStart(GameContext context) {
        World world = context.world();
        for (UUID playerId : context.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            String teamName = playerTeams.get(playerId);
            BeaconTeam team = teams.get(teamName);
            Location spawn = findSafeSpawn(team.beaconLocation().clone().add(0.5, 1.0, 0.5), 5);
            player.teleport(spawn);
            alivePlayers.add(playerId);
            new CompassItem().use(player);
            player.setCompassTarget(ringCenter);
            player.sendMessage("Your team: " + teamName + ". Protect your beacon.");
        }
        startBeamTask(world);
        startResistanceTask();
    }

    @Override
    public void onEnd(GameSession session) {
        if (beamTask != null) {
            beamTask.cancel();
        }
        if (resistanceTask != null) {
            resistanceTask.cancel();
        }
    }

    @Override
    public void onBlockBreak(GameSession session, BlockBreakEvent event) {
        BeaconTeam targetTeam = findTeamByBeacon(event.getBlock());
        if (targetTeam == null) {
            return;
        }
        String attackerTeam = playerTeams.get(event.getPlayer().getUniqueId());
        if (Objects.equals(attackerTeam, targetTeam.name())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You cannot destroy your own beacon.");
            return;
        }

        targetTeam.setBeaconAlive(false);
        event.setDropItems(false);
        event.getBlock().setType(Material.AIR, false);
        Bukkit.broadcastMessage(targetTeam.name() + " beacon has been destroyed.");

        if (!firstBeaconDestroyed) {
            firstBeaconDestroyed = true;
            BeaconTeam centerTeam = teams.get(attackerTeam);
            if (centerTeam != null && centerTeam.beaconAlive()) {
                ringCenter = centerTeam.beaconLocation().clone().add(0.5, 0.0, 0.5);
                borderManager.configure(session.world(), ringCenter, 280.0);
                borderManager.shrink(session.world(), 70.0, 600L);
                session.world().getWorldBorder().setDamageBuffer(0.0);
                session.world().getWorldBorder().setDamageAmount(1.0);
                Bukkit.broadcastMessage("The first beacon fell. The ring now centers on " + attackerTeam + ".");
                Bukkit.broadcastMessage("Nether and End are no longer safe.");
            }
        }
        checkForVictory();
    }

    @Override
    public void onBlockPlace(GameSession session, BlockPlaceEvent event) {
        if (session.players().contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerDeath(GameSession session, PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerId = player.getUniqueId();
        if (!session.players().contains(playerId)) {
            return;
        }
        event.getDrops().clear();
        event.setKeepInventory(true);
        event.setKeepLevel(true);
        alivePlayers.remove(playerId);

        BeaconTeam team = teams.get(playerTeams.get(playerId));
        if (team != null && team.beaconAlive()) {
            respawningPlayers.add(playerId);
            Location respawnLocation = findSafeSpawn(team.beaconLocation().clone().add(0.5, 1.0, 0.5), 5);
            pendingRespawns.put(playerId, respawnLocation);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                Player online = Bukkit.getPlayer(playerId);
                if (online == null || !team.beaconAlive()) {
                    return;
                }
                respawningPlayers.remove(playerId);
                pendingRespawns.remove(playerId);
                online.setGameMode(org.bukkit.GameMode.SURVIVAL);
                online.teleport(respawnLocation);
                online.setHealth(online.getMaxHealth());
                online.setFoodLevel(20);
                alivePlayers.add(playerId);
                new CompassItem().use(online);
                online.setCompassTarget(ringCenter);
                online.sendMessage("You have respawned at your beacon.");
                checkForVictory();
            }, 30L * 20L);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, this::checkForVictory, 2L);
        }
    }

    @Override
    public void onPlayerRespawn(GameSession session, PlayerRespawnEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        if (!session.players().contains(playerId)) {
            return;
        }
        Location respawnLocation = pendingRespawns.getOrDefault(playerId, session.world().getSpawnLocation());
        event.setRespawnLocation(respawnLocation);
        Bukkit.getScheduler().runTask(plugin, () -> event.getPlayer().setGameMode(org.bukkit.GameMode.SPECTATOR));
    }

    @Override
    public void onPlayerLeave(GameSession session, UUID playerId) {
        alivePlayers.remove(playerId);
        respawningPlayers.remove(playerId);
        pendingRespawns.remove(playerId);
        checkForVictory();
    }

    private Map<String, Set<UUID>> buildTeams(GameSession session) {
        Map<String, Set<UUID>> grouped = new LinkedHashMap<>();
        int fallback = 1;
        for (UUID playerId : session.players()) {
            String teamName = session.teamSelections().get(playerId);
            if (teamName == null || teamName.isBlank()) {
                teamName = "Team " + fallback++;
            }
            grouped.computeIfAbsent(teamName, ignored -> new LinkedHashSet<>()).add(playerId);
        }
        return grouped;
    }

    private Location prepareBeaconLocation(World world, int x, int z) {
        int y = world.getHighestBlockYAt(x, z);
        Location beacon = new Location(world, x, y + 1, z);
        beacon.getBlock().setType(Material.BEACON, false);
        for (int up = 1; up <= 3; up++) {
            beacon.clone().add(0, up, 0).getBlock().setType(Material.AIR, false);
        }
        return beacon.toBlockLocation();
    }

    private Location findSafeSpawn(Location origin, int radius) {
        World world = origin.getWorld();
        if (world == null) {
            return origin;
        }
        for (int distance = 0; distance <= radius; distance++) {
            for (int x = -distance; x <= distance; x++) {
                for (int z = -distance; z <= distance; z++) {
                    Location location = origin.clone().add(x, 0, z);
                    Block feet = location.getBlock();
                    Block head = location.clone().add(0, 1, 0).getBlock();
                    Block below = location.clone().add(0, -1, 0).getBlock();
                    if (feet.isPassable() && head.isPassable() && !below.isPassable()) {
                        return location;
                    }
                }
            }
        }
        return origin;
    }

    private BeaconTeam findTeamByBeacon(Block block) {
        for (BeaconTeam team : teams.values()) {
            if (team.beaconAlive() && team.beaconLocation().toBlockLocation().equals(block.getLocation().toBlockLocation())) {
                return team;
            }
        }
        return null;
    }

    private void startBeamTask(World world) {
        beamTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (BeaconTeam team : teams.values()) {
                if (!team.beaconAlive()) {
                    continue;
                }
                Location base = team.beaconLocation().clone().add(0.5, 1.0, 0.5);
                for (int y = 0; y <= 30; y += 2) {
                    world.spawnParticle(Particle.END_ROD, base.clone().add(0.0, y, 0.0), 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }, 1L, 10L);
    }

    private void startResistanceTask() {
        resistanceTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID playerId : alivePlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player == null) {
                    continue;
                }
                BeaconTeam team = teams.get(playerTeams.get(playerId));
                if (team == null || !team.beaconAlive()) {
                    continue;
                }
                if (player.getWorld().equals(team.beaconLocation().getWorld())
                        && player.getLocation().distance(team.beaconLocation().clone().add(0.5, 0.5, 0.5)) <= 12.0) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, true, false, true));
                }
            }
        }, 20L, 20L);
    }

    private void checkForVictory() {
        Set<String> survivingTeams = new LinkedHashSet<>();
        for (UUID playerId : alivePlayers) {
            String teamName = playerTeams.get(playerId);
            if (teamName != null) {
                survivingTeams.add(teamName);
            }
        }
        for (UUID playerId : respawningPlayers) {
            String teamName = playerTeams.get(playerId);
            if (teamName != null) {
                survivingTeams.add(teamName);
            }
        }
        if (survivingTeams.size() == 1) {
            gameManager.endGame(survivingTeams.iterator().next() + " wins Beacon Defence War.");
        }
    }

    private static final class BeaconTeam {
        private final String name;
        private final Location beaconLocation;
        private final Set<UUID> members;
        private boolean beaconAlive = true;

        private BeaconTeam(String name, Location beaconLocation, Set<UUID> members) {
            this.name = name;
            this.beaconLocation = beaconLocation;
            this.members = members;
        }

        public String name() {
            return name;
        }

        public Location beaconLocation() {
            return beaconLocation;
        }

        public Set<UUID> members() {
            return members;
        }

        public boolean beaconAlive() {
            return beaconAlive;
        }

        public void setBeaconAlive(boolean beaconAlive) {
            this.beaconAlive = beaconAlive;
        }
    }
}
