package mygameplugin.gui;

import java.util.Map;
import mygameplugin.gamemode.FeatureFlag;
import mygameplugin.gamemode.GameModeType;
import mygameplugin.gamemode.GameSession;
import mygameplugin.gamemode.OptionState;
import mygameplugin.gamemode.TeamAssignMode;
import mygameplugin.gamemode.WorldMode;
import mygameplugin.manager.GameManager;
import mygameplugin.manager.LobbyManager;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;

public class GameGUI {
    public static final Map<String, DyeColor> TEAM_COLORS = Map.of(
            "Red", DyeColor.RED,
            "Blue", DyeColor.BLUE,
            "Green", DyeColor.GREEN,
            "Yellow", DyeColor.YELLOW,
            "Purple", DyeColor.PURPLE,
            "Orange", DyeColor.ORANGE);

    private final GameManager gameManager;
    private final LobbyManager lobbyManager;
    private final MainMenuGUI mainMenuGUI = new MainMenuGUI();
    private final GameSelectGUI gameSelectGUI = new GameSelectGUI();
    private final FeatureSelectGUI featureSelectGUI = new FeatureSelectGUI();
    private final LobbyGUI lobbyGUI = new LobbyGUI();

    public GameGUI(GameManager gameManager, LobbyManager lobbyManager) {
        this.gameManager = gameManager;
        this.lobbyManager = lobbyManager;
    }

    public void openMain(Player player) {
        mainMenuGUI.open(player, gameManager.hasSession(), gameManager.hasLobby() || gameManager.isSetupOpen());
    }

    public void openGameSelect(Player player) {
        if (!gameManager.hasSession()) {
            gameManager.createSession(player);
        }
        gameSelectGUI.open(player, gameManager.getCurrentSession().mode());
    }

    public void openFeatureSelect(Player player) {
        GameSession session = gameManager.getCurrentSession();
        featureSelectGUI.open(player, session);
    }

    public void openLobby(Player player) {
        lobbyGUI.open(player, gameManager);
    }

    public void handleClick(Player player, InventoryView view, int slot, Material clickedType) {
        String title = view.getTitle();
        if (MainMenuGUI.TITLE.equals(title)) {
            handleMainMenu(player, slot);
            return;
        }
        if (GameSelectGUI.TITLE.equals(title)) {
            handleGameSelect(player, slot);
            return;
        }
        if (FeatureSelectGUI.TITLE.equals(title)) {
            handleFeatureSelect(player, slot);
            return;
        }
        if (LobbyGUI.TITLE.equals(title)) {
            handleLobby(player, slot, clickedType);
        }
    }

    private void handleMainMenu(Player player, int slot) {
        if (slot == 11) {
            if (gameManager.hasSession() && !gameManager.getLobbyPlayers().contains(player.getUniqueId())) {
                player.sendMessage("A game is already being configured.");
                return;
            }
            openGameSelect(player);
            return;
        }
        if (slot == 15) {
            if (lobbyManager.joinLobby(player)) {
                openLobby(player);
            } else {
                player.sendMessage("No joinable lobby is available.");
            }
        }
    }

    private void handleGameSelect(Player player, int slot) {
        GameModeType mode = switch (slot) {
            case 20 -> GameModeType.BEACON_DEFENCE_WAR;
            case 24 -> GameModeType.SURVIVAL_PVP;
            default -> null;
        };
        if (mode != null) {
            gameManager.setMode(mode);
            openFeatureSelect(player);
            return;
        }
        if (slot == 49) {
            openMain(player);
        }
    }

    private void handleFeatureSelect(Player player, int slot) {
        GameSession session = gameManager.getCurrentSession();
        GameModeType mode = session.mode();
        switch (slot) {
            case 10 -> toggleFeature(player, FeatureFlag.BORDER);
            case 11 -> toggleFeature(player, FeatureFlag.ABILITY);
            case 12 -> toggleFeature(player, FeatureFlag.SPECIAL_ITEMS);
            case 19 -> {
                chooseWorldMode(mode, WorldMode.TEMPORARY);
                openFeatureSelect(player);
            }
            case 20 -> {
                chooseWorldMode(mode, WorldMode.FIXED_MAP);
                openFeatureSelect(player);
            }
            case 28 -> {
                chooseTeamMode(mode, TeamAssignMode.ASSIGN);
                openFeatureSelect(player);
            }
            case 29 -> {
                chooseTeamMode(mode, TeamAssignMode.RANDOM);
                openFeatureSelect(player);
            }
            case 45 -> openGameSelect(player);
            case 53 -> {
                gameManager.finishSetupAndOpenLobby();
                openLobby(player);
            }
            default -> {
            }
        }
    }

    private void toggleFeature(Player player, FeatureFlag featureFlag) {
        OptionState rule = gameManager.getCurrentSession().mode().featureRule(featureFlag);
        if (rule.locked()) {
            player.sendMessage(featureFlag.displayName() + " is locked for this mode.");
            return;
        }
        gameManager.toggleFeature(featureFlag);
        openFeatureSelect(player);
    }

    private void chooseWorldMode(GameModeType mode, WorldMode worldMode) {
        if (!mode.supportsWorldMode(worldMode)) {
            return;
        }
        gameManager.setWorldMode(worldMode);
    }

    private void chooseTeamMode(GameModeType mode, TeamAssignMode teamAssignMode) {
        if (!mode.supportsTeamMode(teamAssignMode)) {
            return;
        }
        gameManager.setTeamAssignMode(teamAssignMode);
    }

    private void handleLobby(Player player, int slot, Material clickedType) {
        if (slot == 45) {
            if (gameManager.startGame(player)) {
                player.closeInventory();
            }
            return;
        }
        if (slot == 49) {
            if (gameManager.leaveFromGame(player)) {
                player.closeInventory();
            }
            return;
        }
        if (slot == 47 && gameManager.getCurrentSession().mode().supportsTeamMode(TeamAssignMode.ASSIGN)
                && gameManager.getCurrentSession().mode().supportsTeamMode(TeamAssignMode.RANDOM)) {
            TeamAssignMode next = gameManager.getTeamAssignMode() == TeamAssignMode.ASSIGN ? TeamAssignMode.RANDOM : TeamAssignMode.ASSIGN;
            gameManager.setTeamAssignMode(next);
            openLobby(player);
            return;
        }
        if (clickedType.name().endsWith("_WOOL") && gameManager.getTeamAssignMode() == TeamAssignMode.ASSIGN) {
            TEAM_COLORS.entrySet().stream()
                    .filter(entry -> (entry.getValue().name() + "_WOOL").equals(clickedType.name()))
                    .findFirst()
                    .ifPresent(entry -> {
                        gameManager.selectTeamColor(player, entry.getKey());
                        openLobby(player);
                    });
        }
    }
}
