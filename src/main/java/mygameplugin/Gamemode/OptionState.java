package mygameplugin.gamemode;

public enum OptionState {
    FORCED("Forced", true, false),
    OPTIONAL("Optional", false, false),
    DISABLED("Not Allowed", false, true);

    private final String description;
    private final boolean enabled;
    private final boolean locked;

    OptionState(String description, boolean enabled, boolean locked) {
        this.description = description;
        this.enabled = enabled;
        this.locked = locked;
    }

    public String symbol() {
        return switch (this) {
            case FORCED -> "[ON]";
            case OPTIONAL -> "[ ]";
            case DISABLED -> "[X]";
        };
    }

    public String description() {
        return description;
    }

    public boolean enabled() {
        return enabled;
    }

    public boolean locked() {
        return locked;
    }
}
