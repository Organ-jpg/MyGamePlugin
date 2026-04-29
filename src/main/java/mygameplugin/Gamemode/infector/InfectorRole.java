package mygameplugin.gamemode.infector;

public enum InfectorRole {
    QUEEN("INFECTED QUEEN", "Lead the swarm"),
    INFECTED("INFECTED", "Spread the infection"),
    RUNNER("RUNNER", "Escape and survive");

    private final String title;
    private final String subtitle;

    InfectorRole(String title, String subtitle) {
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
