package mygameplugin.gamemode;

public enum WorldMode {
    TEMPORARY("Temporary World"),
    FIXED_MAP("Fixed Map");

    private final String displayName;

    WorldMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
