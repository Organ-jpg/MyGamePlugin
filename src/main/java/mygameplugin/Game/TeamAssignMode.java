package mygameplugin.gamemode;

public enum TeamAssignMode {
    ASSIGN("Assign Team"),
    RANDOM("Random Team");

    private final String displayName;

    TeamAssignMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
