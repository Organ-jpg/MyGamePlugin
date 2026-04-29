package mygameplugin.gamemode;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.World;

public final class GameSession {
    private UUID host;
    private final Set<UUID> players = new LinkedHashSet<>();
    private final Set<FeatureFlag> enabledFeatures = EnumSet.noneOf(FeatureFlag.class);
    private final Map<UUID, String> teamSelections = new LinkedHashMap<>();
    private GameModeType mode = GameModeType.IMPOSTER;
    private GameState state = GameState.SETUP;
    private WorldMode worldMode = WorldMode.TEMPORARY;
    private TeamAssignMode teamAssignMode = TeamAssignMode.RANDOM;
    private World world;

    private GameSession(UUID host) {
        this.host = host;
        normalizeRules();
    }

    public static GameSession create(UUID host) {
        return new GameSession(host);
    }

    public UUID host() {
        return host;
    }

    public void host(UUID host) {
        this.host = host;
    }

    public Set<UUID> players() {
        return players;
    }

    public Set<FeatureFlag> enabledFeatures() {
        return enabledFeatures;
    }

    public Map<UUID, String> teamSelections() {
        return teamSelections;
    }

    public GameModeType mode() {
        return mode;
    }

    public void mode(GameModeType mode) {
        this.mode = mode;
        normalizeRules();
    }

    public GameState state() {
        return state;
    }

    public void state(GameState state) {
        this.state = state;
    }

    public WorldMode worldMode() {
        return worldMode;
    }

    public void world(World world) {
        this.world = world;
    }

    public World world() {
        return world;
    }

    public TeamAssignMode teamAssignMode() {
        return teamAssignMode;
    }

    public void toggleFeature(FeatureFlag featureFlag) {
        OptionState rule = mode.featureRule(featureFlag);
        if (rule.locked()) {
            return;
        }
        if (enabledFeatures.contains(featureFlag)) {
            enabledFeatures.remove(featureFlag);
        } else {
            enabledFeatures.add(featureFlag);
        }
    }

    public void setWorldMode(WorldMode worldMode) {
        if (mode.worldRule(worldMode).locked() && mode.worldRule(worldMode) == OptionState.DISABLED) {
            return;
        }
        if (mode.supportsWorldMode(worldMode)) {
            this.worldMode = worldMode;
        }
        normalizeRules();
    }

    public void setTeamAssignMode(TeamAssignMode teamAssignMode) {
        if (mode.teamRule(teamAssignMode).locked() && mode.teamRule(teamAssignMode) == OptionState.DISABLED) {
            return;
        }
        if (mode.supportsTeamMode(teamAssignMode)) {
            this.teamAssignMode = teamAssignMode;
        }
        normalizeRules();
    }

    public boolean hasFeature(FeatureFlag featureFlag) {
        return enabledFeatures.contains(featureFlag);
    }

    public void normalizeRules() {
        for (FeatureFlag featureFlag : FeatureFlag.values()) {
            OptionState rule = mode.featureRule(featureFlag);
            if (rule == OptionState.FORCED) {
                enabledFeatures.add(featureFlag);
            } else if (rule == OptionState.DISABLED) {
                enabledFeatures.remove(featureFlag);
            }
        }

        worldMode = pickWorldMode();
        teamAssignMode = pickTeamMode();

        if (teamAssignMode != TeamAssignMode.ASSIGN) {
            teamSelections.clear();
        }
    }

    public boolean supportsTeamMode(TeamAssignMode candidate) {
        return mode.supportsTeamMode(candidate);
    }

    public void assignRandomTeams(List<UUID> onlinePlayers) {
        teamSelections.clear();
        List<String> colors = new ArrayList<>(List.of("Red", "Blue", "Green", "Yellow"));
        for (int index = 0; index < onlinePlayers.size(); index++) {
            teamSelections.put(onlinePlayers.get(index), colors.get(index % colors.size()));
        }
    }

    private WorldMode pickWorldMode() {
        if (mode.worldRule(WorldMode.TEMPORARY) == OptionState.FORCED) {
            return WorldMode.TEMPORARY;
        }
        if (mode.worldRule(WorldMode.FIXED_MAP) == OptionState.FORCED) {
            return WorldMode.FIXED_MAP;
        }
        if (!mode.supportsWorldMode(worldMode)) {
            return mode.supportsWorldMode(WorldMode.TEMPORARY) ? WorldMode.TEMPORARY : WorldMode.FIXED_MAP;
        }
        return worldMode;
    }

    private TeamAssignMode pickTeamMode() {
        if (mode.teamRule(TeamAssignMode.ASSIGN) == OptionState.FORCED) {
            return TeamAssignMode.ASSIGN;
        }
        if (mode.teamRule(TeamAssignMode.RANDOM) == OptionState.FORCED) {
            return TeamAssignMode.RANDOM;
        }
        if (!mode.supportsTeamMode(teamAssignMode)) {
            return mode.supportsTeamMode(TeamAssignMode.ASSIGN) ? TeamAssignMode.ASSIGN : TeamAssignMode.RANDOM;
        }
        return teamAssignMode;
    }
}
