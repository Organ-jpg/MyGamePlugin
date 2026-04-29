package mygameplugin.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import mygameplugin.MyGamePlugin;
import mygameplugin.gamemode.FeatureFlag;
import mygameplugin.gamemode.GameContext;
import mygameplugin.gamemode.GameModeType;
import mygameplugin.gamemode.GameSession;
import mygameplugin.gamemode.GameState;
import mygameplugin.gamemode.TeamAssignMode;
import mygameplugin.gamemode.WorldMode;
import mygameplugin.gamemode.runtime.BeaconDefenceWarRuntime;
import mygameplugin.gamemode.runtime.GameRuntime;
import mygameplugin.gamemode.runtime.ImposterRuntime;
import mygameplugin.gamemode.runtime.InfectorRuntime;
import mygameplugin.gamemode.runtime.PvPStadiumRuntime;
import mygameplugin.gamemode.runtime.SimpleGameRuntime;
import mygameplugin.gamemode.runtime.SurvivalPvPRuntime;
import mygameplugin.world.FixedMapWorldService;
import mygameplugin.world.TemporaryWorldService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class GameManager {
    private final MyGamePlugin plugin;
    private final TemporaryWorldService temporaryWorldService;
    private final FixedMapWorldService fixedMapWorldService;
    private final Map<UUID, PlayerSnapshot> snapshots = new LinkedHashMap<>();
    private GameSession currentSession;
    private GameRuntime runtime;
    private BukkitTask countdownTask;

    public GameManager(MyGamePlugin plugin, TemporaryWorldService temporaryWorldService, FixedMapWorldService fixedMapWorldService) {
        this.plugin = plugin;
        this.temporaryWorldService = temporaryWorldService;
        this.fixedMapWorldService = fixedMapWorldService;
    }

    public GameState getState() {
        return currentSession == null ? GameState.IDLE : currentSession.state();
    }

    public boolean hasSession() {
        return currentSession != null;
    }

    public boolean isSetupOpen() {
        return currentSession != null && currentSession.state() == GameState.SETUP;
    }

    public boolean hasLobby() {
        return currentSession != null && currentSession.state() == GameState.LOBBY;
    }

    public GameSession getCurrentSession() {
        return currentSession;
    }

    public UUID getHost() {
        return currentSession == null ? null : currentSession.host();
    }

    public Set<UUID> getLobbyPlayers() {
        return currentSession == null ? Collections.emptySet() : Collections.unmodifiableSet(currentSession.players());
    }

    public TeamAssignMode getTeamAssignMode() {
        return currentSession == null ? TeamAssignMode.RANDOM : currentSession.teamAssignMode();
    }

    public boolean shouldLockInventory() {
        return currentSession != null && (currentSession.state() == GameState.STARTING || currentSession.state() == GameState.PREPARING);
    }

    public GameModeType getSelectedMode() {
        return currentSession == null ? null : currentSession.mode();
    }

    public boolean isCommandRestricted(Player player) {
        if (player.isOp()) {
            return false;
        }
        return currentSession != null && currentSession.players().contains(player.getUniqueId()) && currentSession.state() == GameState.INGAME;
    }

    public boolean createSession(Player host) {
        if (currentSession != null) {
            return false;
        }
        currentSession = GameSession.create(host.getUniqueId());
        currentSession.players().add(host.getUniqueId());
        return true;
    }

    public boolean canJoin(Player player) {
        return currentSession != null
                && (currentSession.state() == GameState.SETUP || currentSession.state() == GameState.LOBBY)
                && !currentSession.players().contains(player.getUniqueId());
    }

    public boolean joinLobby(Player player) {
        if (!canJoin(player)) {
            return false;
        }
        currentSession.players().add(player.getUniqueId());
        player.sendMessage("Joined " + currentSession.mode().displayName() + " lobby.");
        broadcastLobbyState();
        return true;
    }

    public boolean leaveFromGame(Player player) {
        if (currentSession == null || !currentSession.players().contains(player.getUniqueId())) {
            return false;
        }

        UUID playerId = player.getUniqueId();
        currentSession.players().remove(playerId);
        currentSession.teamSelections().remove(playerId);
        restorePlayer(player);

        if (runtime != null) {
            runtime.onPlayerLeave(currentSession, playerId);
        }

        if (currentSession.players().isEmpty()) {
            endGame("Everyone left the match.");
        } else if (Objects.equals(currentSession.host(), playerId)) {
            UUID nextHost = currentSession.players().iterator().next();
            currentSession.host(nextHost);
            broadcast("Host left. " + nameOf(nextHost) + " is now the host.");
        }
        return true;
    }

    public void setMode(GameModeType mode) {
        ensureSession();
        currentSession.mode(mode);
        currentSession.normalizeRules();
    }

    public void toggleFeature(FeatureFlag featureFlag) {
        ensureSession();
        currentSession.toggleFeature(featureFlag);
    }

    public void setWorldMode(WorldMode worldMode) {
        ensureSession();
        currentSession.setWorldMode(worldMode);
    }

    public void setTeamAssignMode(TeamAssignMode teamAssignMode) {
        ensureSession();
        currentSession.setTeamAssignMode(teamAssignMode);
    }

    public void selectTeamColor(Player player, String colorKey) {
        ensureSession();
        if (currentSession.teamAssignMode() != TeamAssignMode.ASSIGN || !currentSession.mode().supportsTeamMode(TeamAssignMode.ASSIGN)) {
            return;
        }
        currentSession.teamSelections().put(player.getUniqueId(), colorKey);
    }

    public boolean finishSetupAndOpenLobby() {
        ensureSession();
        currentSession.state(GameState.LOBBY);
        broadcastLobbyState();
        return true;
    }

    public boolean startGame(Player player) {
        ensureSession();
        if (!Objects.equals(currentSession.host(), player.getUniqueId()) && !player.isOp()) {
            player.sendMessage("Only the host can start the game.");
            return false;
        }
        if (currentSession.state() != GameState.LOBBY) {
            player.sendMessage("The lobby is not ready yet.");
            return false;
        }
        if (!player.isOp() && currentSession.players().size() < currentSession.mode().minPlayers()) {
            player.sendMessage("This mode needs at least " + currentSession.mode().minPlayers() + " players.");
            return false;
        }

        currentSession.state(GameState.STARTING);
        broadcast("Starting " + currentSession.mode().displayName() + " in 8 seconds...");
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        countdownTask = Bukkit.getScheduler().runTaskLater(plugin, this::prepareGame, 8L * 20L);
        return true;
    }

    private void prepareGame() {
        if (currentSession == null) {
            return;
        }
        currentSession.state(GameState.PREPARING);
        savePlayers();
        if (currentSession.mode().announcesWorldGeneration()) {
            broadcast("Generating world...");
        }
        if (currentSession.teamAssignMode() == TeamAssignMode.RANDOM && currentSession.mode().hasTeams()) {
            currentSession.assignRandomTeams(new ArrayList<>(currentSession.players()));
        }

        World world = currentSession.worldMode() == WorldMode.TEMPORARY
                ? temporaryWorldService.createMatchWorld(currentSession.mode())
                : fixedMapWorldService.prepareWorld(currentSession.mode());
        currentSession.world(world);
        runtime = createRuntime(currentSession.mode());
        runtime.onPreparing(new GameContext(plugin, currentSession.mode(), world, Set.copyOf(currentSession.players()), currentSession));

        Bukkit.getScheduler().runTaskLater(plugin, this::beginIngame, 40L);
    }

    private void beginIngame() {
        if (currentSession == null || currentSession.world() == null) {
            return;
        }
        currentSession.state(GameState.INGAME);
        runtime.onStart(new GameContext(plugin, currentSession.mode(), currentSession.world(), Set.copyOf(currentSession.players()), currentSession));
        if (!runtime.managesSpawns()) {
            teleportPlayersForMatch(currentSession.world());
        }
        broadcast(currentSession.mode().displayName() + " has started.");
    }

    public void endGame(String reason) {
        if (currentSession == null) {
            return;
        }
        currentSession.state(GameState.ENDING);
        broadcast(reason);
        if (runtime != null) {
            runtime.onEnd(currentSession);
        }
        for (UUID playerId : List.copyOf(currentSession.players())) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                restorePlayer(player);
            }
        }
        cleanup();
    }

    private void cleanup() {
        if (currentSession == null) {
            return;
        }
        currentSession.state(GameState.CLEANUP);
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        if (currentSession.worldMode() == WorldMode.TEMPORARY) {
            temporaryWorldService.unloadAndDelete(currentSession.world());
        }
        snapshots.clear();
        runtime = null;
        currentSession = null;
    }

    public void handleDisconnect(Player player) {
        if (currentSession == null || !currentSession.players().contains(player.getUniqueId())) {
            return;
        }
        if (currentSession.state() == GameState.INGAME) {
            broadcast(player.getName() + " disconnected and can rejoin if the match is still active.");
        } else if (currentSession.state() == GameState.LOBBY || currentSession.state() == GameState.SETUP) {
            currentSession.players().remove(player.getUniqueId());
        }
    }

    public void handleReconnect(Player player) {
        if (currentSession == null || currentSession.state() != GameState.INGAME) {
            return;
        }
        if (currentSession.players().contains(player.getUniqueId()) && currentSession.world() != null) {
            player.teleport(currentSession.world().getSpawnLocation());
            runtime.onPlayerRejoin(currentSession, player);
        }
    }

    public List<String> buildLobbyLines() {
        if (currentSession == null) {
            return List.of();
        }
        return currentSession.players().stream()
                .map(uuid -> {
                    String color = currentSession.teamSelections().get(uuid);
                    return color == null ? nameOf(uuid) : nameOf(uuid) + " [" + color + "]";
                })
                .collect(Collectors.toList());
    }

    public String teamColorOf(UUID playerId) {
        return currentSession == null ? null : currentSession.teamSelections().get(playerId);
    }

    public void handleBlockBreak(BlockBreakEvent event) {
        if (currentSession == null || runtime == null || currentSession.state() != GameState.INGAME) {
            return;
        }
        runtime.onBlockBreak(currentSession, event);
    }

    public void handleBlockPlace(BlockPlaceEvent event) {
        if (currentSession == null || runtime == null || currentSession.state() != GameState.INGAME) {
            return;
        }
        runtime.onBlockPlace(currentSession, event);
    }

    public void handlePlayerDeath(PlayerDeathEvent event) {
        if (currentSession == null || runtime == null || currentSession.state() != GameState.INGAME) {
            return;
        }
        runtime.onPlayerDeath(currentSession, event);
    }

    public void handlePlayerRespawn(PlayerRespawnEvent event) {
        if (currentSession == null || runtime == null || currentSession.state() != GameState.INGAME) {
            return;
        }
        runtime.onPlayerRespawn(currentSession, event);
    }

    private void savePlayers() {
        for (UUID playerId : currentSession.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            snapshots.put(playerId, PlayerSnapshot.capture(player));
            player.getInventory().clear();
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
        }
    }

    private void teleportPlayersForMatch(World world) {
        List<Player> onlinePlayers = new ArrayList<>();
        for (UUID playerId : currentSession.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                onlinePlayers.add(player);
            }
        }

        if (currentSession.teamAssignMode() == TeamAssignMode.RANDOM && currentSession.mode().hasTeams()) {
            currentSession.assignRandomTeams(onlinePlayers.stream().map(Player::getUniqueId).toList());
        }

        Location baseSpawn = world.getSpawnLocation().clone().add(0.5, 1, 0.5);
        int radius = Math.max(6, 6 + onlinePlayers.size() * 2);
        for (int i = 0; i < onlinePlayers.size(); i++) {
            Player player = onlinePlayers.get(i);
            Location spawn = currentSession.mode().spreadsPlayers()
                    ? baseSpawn.clone().add((i % 2 == 0 ? 1 : -1) * radius, 0, (i - onlinePlayers.size() / 2.0) * 3)
                    : baseSpawn.clone();
            player.teleport(spawn);
        }
    }

    private void restorePlayer(Player player) {
        PlayerSnapshot snapshot = snapshots.remove(player.getUniqueId());
        if (snapshot == null) {
            return;
        }
        snapshot.restore(player);
    }

    private GameRuntime createRuntime(GameModeType modeType) {
        return switch (modeType) {
            case IMPOSTER -> new ImposterRuntime(plugin);
            case INFECTOR -> new InfectorRuntime(plugin);
            case BEACON_DEFENCE_WAR -> new BeaconDefenceWarRuntime(plugin, this);
            case SURVIVAL_PVP -> new SurvivalPvPRuntime(plugin, this);
            case PVP_STADIUM -> new PvPStadiumRuntime();
            case TIME_SIEGE -> new SimpleGameRuntime("Time Siege");
        };
    }

    private void broadcastLobbyState() {
        broadcast("Lobby ready. Host can start from the GUI.");
    }

    private void broadcast(String message) {
        if (currentSession == null) {
            return;
        }
        for (UUID playerId : currentSession.players()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    private String nameOf(UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            return player.getName();
        }
        return Objects.toString(Bukkit.getOfflinePlayer(playerId).getName(), playerId.toString());
    }

    private void ensureSession() {
        if (currentSession == null) {
            throw new IllegalStateException("No active session");
        }
    }

    private record PlayerSnapshot(
            Location location,
            GameMode gameMode,
            ItemStack[] inventory,
            ItemStack[] armor,
            double health,
            int foodLevel,
            float exp,
            int level) {

        static PlayerSnapshot capture(Player player) {
            return new PlayerSnapshot(
                    player.getLocation().clone(),
                    player.getGameMode(),
                    player.getInventory().getContents().clone(),
                    player.getInventory().getArmorContents().clone(),
                    player.getHealth(),
                    player.getFoodLevel(),
                    player.getExp(),
                    player.getLevel());
        }

        void restore(Player player) {
            player.teleport(location);
            player.setGameMode(gameMode);
            player.getInventory().setContents(inventory);
            player.getInventory().setArmorContents(armor);
            player.setHealth(Math.min(player.getMaxHealth(), health));
            player.setFoodLevel(foodLevel);
            player.setExp(exp);
            player.setLevel(level);
        }
    }
}
