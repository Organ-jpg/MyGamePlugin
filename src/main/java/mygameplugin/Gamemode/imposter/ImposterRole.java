package mygameplugin.gamemode.imposter;

public enum ImposterRole {
    IMPOSTER("IMPOSTER", "Secret saboteur"),
    INNOCENT("INNOCENT", "Find the dragon");

    private final String title;
    private final String subtitle;

    ImposterRole(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    public String title() {
        return title;
    }

    public String subtitle() {
        return subtitle;
    }
}
