package mygameplugin.gamemode;

public enum FeatureFlag {
    BORDER("Border"),
    ABILITY("Ability"),
    SPECIAL_ITEMS("Special Items");

    private final String displayName;

    FeatureFlag(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
