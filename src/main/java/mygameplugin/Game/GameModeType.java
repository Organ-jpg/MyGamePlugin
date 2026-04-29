package mygameplugin.gamemode;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Material;

public enum GameModeType {
    IMPOSTER(
            "Imposter",
            Material.ENDER_EYE,
            false,
            true,
            2,
            true,
            mapOf(
                    FeatureFlag.BORDER, OptionState.DISABLED,
                    FeatureFlag.ABILITY, OptionState.DISABLED,
                    FeatureFlag.SPECIAL_ITEMS, OptionState.DISABLED),
            mapOf(
                    WorldMode.TEMPORARY, OptionState.FORCED,
                    WorldMode.FIXED_MAP, OptionState.DISABLED),
            mapOf(
                    TeamAssignMode.ASSIGN, OptionState.DISABLED,
                    TeamAssignMode.RANDOM, OptionState.DISABLED)),
    INFECTOR(
            "Infector",
            Material.ZOMBIE_HEAD,
            false,
            true,
            2,
            true,
            mapOf(
                    FeatureFlag.BORDER, OptionState.DISABLED,
                    FeatureFlag.ABILITY, OptionState.DISABLED,
                    FeatureFlag.SPECIAL_ITEMS, OptionState.DISABLED),
            mapOf(
                    WorldMode.TEMPORARY, OptionState.FORCED,
                    WorldMode.FIXED_MAP, OptionState.DISABLED),
            mapOf(
                    TeamAssignMode.ASSIGN, OptionState.DISABLED,
                    TeamAssignMode.RANDOM, OptionState.DISABLED)),
    BEACON_DEFENCE_WAR(
            "Beacon Defence War",
            Material.BEACON,
            true,
            false,
            2,
            false,
            mapOf(
                    FeatureFlag.BORDER, OptionState.FORCED,
                    FeatureFlag.ABILITY, OptionState.OPTIONAL,
                    FeatureFlag.SPECIAL_ITEMS, OptionState.OPTIONAL),
            mapOf(
                    WorldMode.TEMPORARY, OptionState.OPTIONAL,
                    WorldMode.FIXED_MAP, OptionState.OPTIONAL),
            mapOf(
                    TeamAssignMode.ASSIGN, OptionState.OPTIONAL,
                    TeamAssignMode.RANDOM, OptionState.OPTIONAL)),
    SURVIVAL_PVP(
            "Survival PvP",
            Material.DIAMOND_SWORD,
            true,
            false,
            2,
            false,
            mapOf(
                    FeatureFlag.BORDER, OptionState.FORCED,
                    FeatureFlag.ABILITY, OptionState.OPTIONAL,
                    FeatureFlag.SPECIAL_ITEMS, OptionState.FORCED),
            mapOf(
                    WorldMode.TEMPORARY, OptionState.OPTIONAL,
                    WorldMode.FIXED_MAP, OptionState.OPTIONAL),
            mapOf(
                    TeamAssignMode.ASSIGN, OptionState.OPTIONAL,
                    TeamAssignMode.RANDOM, OptionState.OPTIONAL)),
    PVP_STADIUM(
            "PvP Stadium",
            Material.NETHERITE_SWORD,
            true,
            false,
            2,
            false,
            mapOf(
                    FeatureFlag.BORDER, OptionState.FORCED,
                    FeatureFlag.ABILITY, OptionState.DISABLED,
                    FeatureFlag.SPECIAL_ITEMS, OptionState.FORCED),
            mapOf(
                    WorldMode.TEMPORARY, OptionState.DISABLED,
                    WorldMode.FIXED_MAP, OptionState.FORCED),
            mapOf(
                    TeamAssignMode.ASSIGN, OptionState.OPTIONAL,
                    TeamAssignMode.RANDOM, OptionState.OPTIONAL)),
    TIME_SIEGE(
            "Time Siege",
            Material.CLOCK,
            true,
            false,
            2,
            false,
            mapOf(
                    FeatureFlag.BORDER, OptionState.FORCED,
                    FeatureFlag.ABILITY, OptionState.OPTIONAL,
                    FeatureFlag.SPECIAL_ITEMS, OptionState.FORCED),
            mapOf(
                    WorldMode.TEMPORARY, OptionState.OPTIONAL,
                    WorldMode.FIXED_MAP, OptionState.OPTIONAL),
            mapOf(
                    TeamAssignMode.ASSIGN, OptionState.OPTIONAL,
                    TeamAssignMode.RANDOM, OptionState.OPTIONAL));

    private final String displayName;
    private final Material icon;
    private final boolean hasTeams;
    private final boolean roleSystem;
    private final int minPlayers;
    private final boolean announcesWorldGeneration;
    private final Map<FeatureFlag, OptionState> featureRules;
    private final Map<WorldMode, OptionState> worldRules;
    private final Map<TeamAssignMode, OptionState> teamRules;

    GameModeType(
            String displayName,
            Material icon,
            boolean hasTeams,
            boolean roleSystem,
            int minPlayers,
            boolean announcesWorldGeneration,
            Map<FeatureFlag, OptionState> featureRules,
            Map<WorldMode, OptionState> worldRules,
            Map<TeamAssignMode, OptionState> teamRules) {
        this.displayName = displayName;
        this.icon = icon;
        this.hasTeams = hasTeams;
        this.roleSystem = roleSystem;
        this.minPlayers = minPlayers;
        this.announcesWorldGeneration = announcesWorldGeneration;
        this.featureRules = featureRules;
        this.worldRules = worldRules;
        this.teamRules = teamRules;
    }

    public String displayName() {
        return displayName;
    }

    public Material icon() {
        return icon;
    }

    public boolean hasTeams() {
        return hasTeams;
    }

    public boolean roleSystem() {
        return roleSystem;
    }

    public int minPlayers() {
        return minPlayers;
    }

    public boolean announcesWorldGeneration() {
        return announcesWorldGeneration;
    }

    public boolean spreadsPlayers() {
        return this == IMPOSTER;
    }

    public OptionState featureRule(FeatureFlag featureFlag) {
        return featureRules.get(featureFlag);
    }

    public OptionState worldRule(WorldMode worldMode) {
        return worldRules.get(worldMode);
    }

    public OptionState teamRule(TeamAssignMode teamAssignMode) {
        return teamRules.get(teamAssignMode);
    }

    public boolean supportsFeature(FeatureFlag featureFlag) {
        return featureRule(featureFlag) != OptionState.DISABLED;
    }

    public boolean supportsWorldMode(WorldMode worldMode) {
        return worldRule(worldMode) != OptionState.DISABLED;
    }

    public boolean supportsTeamMode(TeamAssignMode teamAssignMode) {
        return teamRule(teamAssignMode) != OptionState.DISABLED;
    }

    private static <K extends Enum<K>> Map<K, OptionState> mapOf(K key1, OptionState value1, K key2, OptionState value2, K key3, OptionState value3) {
        Map<K, OptionState> map = new EnumMap<>(key1.getDeclaringClass());
        map.put(key1, value1);
        map.put(key2, value2);
        map.put(key3, value3);
        return map;
    }

    private static <K extends Enum<K>> Map<K, OptionState> mapOf(K key1, OptionState value1, K key2, OptionState value2) {
        Map<K, OptionState> map = new EnumMap<>(key1.getDeclaringClass());
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }
}
