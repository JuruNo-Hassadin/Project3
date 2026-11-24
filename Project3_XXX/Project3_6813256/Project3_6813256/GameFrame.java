/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Main window and coordinator for the menu, settings, game, and leaderboard. */
public final class GameFrame extends JFrame {
    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 700;
    private static final String MENU_VIEW = "menu";
    private static final String GAME_VIEW = "game";

    private final MenuClickSoundEffect clickSound = new MenuClickSoundEffect();
    private final LeaderboardStore leaderboardStore = new LeaderboardStore();
    private final GamePanel gamePanel = new GamePanel(clickSound,
            () -> GameFrame.this.settings.isSoundEnabled());
    private final BackgroundMusicPlayer musicPlayer = new BackgroundMusicPlayer();
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel views = new JPanel(cardLayout);
    private final MenuPanel menuPanel;
    private GameSettings settings = GameSettings.defaults();
    private LeaderboardEntry pendingGameOverEntry;
    private boolean currentRunSaved;

    public GameFrame() {
        super("Tower Hop - Pixel Art Starter");
        menuPanel = new MenuPanel(new MenuPanel.Actions() {
            @Override public void onPlay() { showPreStartGame(); }
            @Override public void onLeaderboard() { openLeaderboard(); }
            @Override public void onSettings() { openSettings(); }
            @Override public void onCredits() { openCredits(); }
        }, clickSound, () -> GameFrame.this.settings.isSoundEnabled());
        gamePanel.setOnPauseSettings(this::openSettings);
        gamePanel.setPlayerSkin(settings.getPlayerSkin());
        gamePanel.setOnQuitToMenu(this::returnToMenu);
        gamePanel.setOnGameOver(this::showGameOver);
        gamePanel.setOnDifficultySelected(difficulty ->
                settings = settings.withDifficulty(difficulty));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setMaximumSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
        setSize(1150, 680);
        setLayout(new BorderLayout());
        views.add(menuPanel, MENU_VIEW);
        views.add(buildGameScreen(), GAME_VIEW);
        add(views, BorderLayout.CENTER);
        cardLayout.show(views, MENU_VIEW);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                saveActiveRun();
                gamePanel.stopRepainting();
                musicPlayer.close();
                clickSound.close();
                dispose();
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) {
                Dimension size = getSize();
                int width = Math.min(size.width, MAX_WIDTH);
                int height = Math.min(size.height, MAX_HEIGHT);
                if (width != size.width || height != size.height) {
                    setSize(width, height);
                }
            }
        });
        setLocationRelativeTo(null);
        musicPlayer.setVolume(settings.isMusicMuted() ? 0 : settings.getVolume());
        musicPlayer.startLooping();
    }

    private JPanel buildGameScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(gamePanel, BorderLayout.CENTER);
        return panel;
    }

    private void showPreStartGame() {
        if (!currentRunSaved) {
            saveActiveRun();
        }
        pendingGameOverEntry = null;
        gamePanel.prepareForStart(settings);
        currentRunSaved = false;
        cardLayout.show(views, GAME_VIEW);
        SwingUtilities.invokeLater(() -> gamePanel.requestFocusInWindow());
    }

    private void returnToMenu() {
        if (!currentRunSaved) {
            saveActiveRun();
        }
        cardLayout.show(views, MENU_VIEW);
    }

    private boolean saveActiveRun() {
        if (currentRunSaved) {
            return false;
        }
        LeaderboardEntry entry = gamePanel.finishRun();
        if (entry == null) {
            return false;
        }
        try {
            leaderboardStore.save(entry);
            currentRunSaved = true;
            return true;
        } catch (IOException exception) {
            System.err.println("Could not save leaderboard: " + exception.getMessage());
            return false;
        }
    }

    private void showGameOver(int score) {
        pendingGameOverEntry = null;
        GameOverDialog dialog = new GameOverDialog(this, score,
                settings.getPlayerName(), this::saveGameOverScore);
        dialog.setVisible(true);
        if (dialog.wasSaved()) {
            gamePanel.clearAfterGameOver();
            pendingGameOverEntry = null;
            currentRunSaved = true;
            cardLayout.show(views, MENU_VIEW);
        } else if (dialog.wasDiscarded()) {
            gamePanel.discardRun();
            pendingGameOverEntry = null;
            currentRunSaved = true;
            cardLayout.show(views, MENU_VIEW);
        }
    }

    private String saveGameOverScore(String playerName) {
        if (pendingGameOverEntry == null) {
            pendingGameOverEntry = gamePanel.finishRun(playerName);
        } else {
            pendingGameOverEntry = pendingGameOverEntry.withPlayerName(playerName);
        }
        if (pendingGameOverEntry == null) {
            return "Could not prepare the score record.";
        }
        try {
            leaderboardStore.save(pendingGameOverEntry);
            currentRunSaved = true;
            return null;
        } catch (IOException exception) {
            return "Save failed. Try again.";
        }
    }

    private void openSettings() {
        SettingsDialog dialog = new SettingsDialog(this, settings, musicPlayer::setVolume, clickSound);
        dialog.setVisible(true);
        if (dialog.getResult() != null) {
            settings = dialog.getResult();
            menuPanel.setPlayerSkin(settings.getPlayerSkin());
            gamePanel.setPlayerSkin(settings.getPlayerSkin());
        }
        musicPlayer.setVolume(settings.isMusicMuted() ? 0 : settings.getVolume());
    }

    private void openLeaderboard() {
        new LeaderboardDialog(this, leaderboardStore).setVisible(true);
    }

    private void openCredits() {
        new CreditsDialog(this).setVisible(true);
    }
}
