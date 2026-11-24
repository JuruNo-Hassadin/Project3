package Project3_6813256;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.JPanel;
import javax.swing.Timer;

/** Custom-drawn play area with automatic running and platform collision. */
public final class GamePanel extends JPanel {
    private static final int PLAYER_RENDER_HEIGHT = 64;
    private static final int PICKUP_DRAW_WIDTH = 40;
    private static final int PICKUP_DRAW_HEIGHT = 48;
    private GameSession session;
    private GameSettings pendingSettings;
    private boolean awaitingStart;
    private boolean paused;
    private boolean gameOverShown;
    private Runnable onRunStarted = () -> { };
    private Runnable onPauseSettings = () -> { };
    private Runnable onQuitToMenu = () -> { };
    private IntConsumer onGameOver = score -> { };
    private Consumer<Difficulty> onDifficultySelected = difficulty -> { };
    private final MenuClickSoundEffect clickSound;
    private final BooleanSupplier soundEnabled;
    private final Timer repaintTimer;
    private final JumpSoundEffect jumpSound = new JumpSoundEffect();
    private final StopwatchSoundEffect stopwatchSound = new StopwatchSoundEffect();
    private final GameOverSoundEffect gameOverSound = new GameOverSoundEffect();
    private final Map<PlayerSkin, PlayerAnimator> playerAnimators = createPlayerAnimators();
    private final BufferedImage stopwatchSprite = PixelArt.stopwatch();
    private final BufferedImage platformTile = PixelArt.platform();
    private PlayerSkin playerSkin = PlayerSkin.RUNNER;
    private String message = "Press Start. Some platforms move side to side; Space or click jumps.";
    private long lastUpdateNanos = System.nanoTime();

    private static Map<PlayerSkin, PlayerAnimator> createPlayerAnimators() {
        Map<PlayerSkin, PlayerAnimator> animators = new EnumMap<>(PlayerSkin.class);
        for (PlayerSkin skin : PlayerSkin.values()) {
            int legStartRow = skin == PlayerSkin.RUNNER ? 9
                    : skin == PlayerSkin.SHAYNE ? 25 : 24;
            animators.put(skin, new PlayerAnimator(PixelArt.player(skin), legStartRow));
        }
        return animators;
    }

    public GamePanel(MenuClickSoundEffect clickSound, BooleanSupplier soundEnabled) {
        this.clickSound = clickSound;
        this.soundEnabled = soundEnabled == null ? () -> true : soundEnabled;
        setPreferredSize(new Dimension(1100, 620));
        setFocusable(true);
        setBackground(new Color(202, 231, 247));
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent event) {
                handleKeyPressed(event);
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent event) {
                handleMouseClicked(event);
            }
        });
        repaintTimer = new Timer(16, event -> updateGame());
        repaintTimer.start();
    }

    public void beginRun(GameSettings settings) {
        session = new GameSession(settings);
        session.start();
        pendingSettings = settings;
        playerSkin = settings.getPlayerSkin();
        awaitingStart = false;
        paused = false;
        gameOverShown = false;
        lastUpdateNanos = System.nanoTime();
        message = "Auto-running. Some platforms move side to side. Space jumps; Esc pauses.";
        onRunStarted.run();
        requestFocusInWindow();
        repaint();
    }

    public void setOnRunStarted(Runnable action) {
        onRunStarted = action == null ? () -> { } : action;
    }

    public void setOnPauseSettings(Runnable action) {
        onPauseSettings = action == null ? () -> { } : action;
    }

    public void setOnQuitToMenu(Runnable action) {
        onQuitToMenu = action == null ? () -> { } : action;
    }

    public void setOnGameOver(IntConsumer action) {
        onGameOver = action == null ? score -> { } : action;
    }

    public void setOnDifficultySelected(Consumer<Difficulty> action) {
        onDifficultySelected = action == null ? difficulty -> { } : action;
    }

    public void setPlayerSkin(PlayerSkin selectedSkin) {
        playerSkin = selectedSkin == null ? PlayerSkin.RUNNER : selectedSkin;
        repaint();
    }

    /** Shows the play scene, but waits for the first Spacebar press to start. */
    public void prepareForStart(GameSettings settings) {
        session = new GameSession(settings);
        session.start();
        session.pause();
        pendingSettings = settings;
        playerSkin = settings.getPlayerSkin();
        awaitingStart = true;
        paused = false;
        gameOverShown = false;
        lastUpdateNanos = System.nanoTime();
        message = "Press Spacebar to start. Some platforms move side to side. Escape returns.";
        requestFocusInWindow();
        repaint();
    }

    public LeaderboardEntry finishRun() {
        if (session == null || awaitingStart) {
            return null;
        }
        LeaderboardEntry entry = session.finish();
        if (entry != null) {
            message = "Run saved. Floors climbed: " + entry.floorsClimbed();
        }
        repaint();
        return entry;
    }

    public LeaderboardEntry finishRun(String playerName) {
        if (session == null || awaitingStart) {
            return null;
        }
        LeaderboardEntry entry = session.finish(playerName);
        repaint();
        return entry;
    }

    public void discardRun() {
        if (session != null) {
            session.discard();
        }
        session = null;
        paused = false;
        awaitingStart = false;
        repaint();
    }

    public void clearAfterGameOver() {
        session = null;
        paused = false;
        awaitingStart = false;
        repaint();
    }

    public boolean hasActiveRun() {
        return session != null && session.isRunning() && !awaitingStart;
    }

    public void stopRepainting() {
        repaintTimer.stop();
        jumpSound.close();
        stopwatchSound.close();
        gameOverSound.close();
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (paused) {
                resumeGame();
            } else if (awaitingStart) {
                discardRun();
                onQuitToMenu.run();
            } else if (session != null && session.isRunning()) {
                pauseGame();
            } else if (session != null) {
                onQuitToMenu.run();
            }
            return;
        }
        if (paused) {
            return;
        }
        if (awaitingStart) {
            if (event.getKeyCode() >= KeyEvent.VK_1
                    && event.getKeyCode() <= KeyEvent.VK_5) {
                selectDifficulty(Difficulty.values()[event.getKeyCode() - KeyEvent.VK_1]);
            }
            if (event.getKeyCode() == KeyEvent.VK_SPACE) {
                startPreparedRun();
            }
            return;
        }
        if (session == null || !session.isRunning()) {
            return;
        }
        if (event.getKeyCode() == KeyEvent.VK_SPACE) {
            message = requestJump()
                    ? "Jumping: land on the next platform to climb."
                    : "Wait until you land before jumping again.";
        } else if (event.getKeyCode() == KeyEvent.VK_E) {
            activateStopwatch();
        }
        repaint();
    }

    private void handleMouseClicked(MouseEvent event) {
        if (paused) {
            handlePauseMenuClick(event.getPoint());
            return;
        }
        if (awaitingStart) {
            for (int index = 0; index < Difficulty.values().length; index++) {
                if (difficultyBounds(index).contains(event.getPoint())) {
                    playMenuClick();
                    selectDifficulty(Difficulty.values()[index]);
                    requestFocusInWindow();
                    repaint();
                    return;
                }
            }
            requestFocusInWindow();
            return;
        }
        if (session == null || !session.isRunning()) {
            requestFocusInWindow();
            return;
        }
        if (skillBounds().contains(event.getPoint())) {
            activateStopwatch();
        } else {
            message = requestJump()
                    ? "Jumping: land on the next platform to climb."
                    : "Wait until you land before jumping again.";
        }
        requestFocusInWindow();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        drawBackground(g);
        drawPlatforms(g);
        drawStopwatchPickups(g);
        drawHud(g);
        drawPlayer(g);
        if (awaitingStart) {
            drawStartPrompt(g);
            drawDifficultySelector(g);
        }
        if (paused) {
            drawPauseOverlay(g);
        }
        g.dispose();
    }

    private void drawBackground(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(PixelArt.background(), 0, 0, getWidth(), getHeight(), null);
    }

    private void drawPlatforms(Graphics2D g) {
        List<Platform> visiblePlatforms = session == null ? List.of() : session.getPlatforms();
        for (Platform platform : visiblePlatforms) {
            int screenX = worldToScreenX(platform.getX());
            int screenY = worldToScreenY(platform.getY());
            if (screenX + platform.getWidth() < 0 || screenX > getWidth()
                    || screenY < -24 || screenY > getHeight() + 4) {
                continue;
            }
            Shape previousClip = g.getClip();
            g.clipRect(screenX, screenY, platform.getWidth(), 22);
            for (int x = screenX; x < screenX + platform.getWidth(); x += 32) {
                g.drawImage(platformTile, x, screenY, 32, 22, null);
            }
            g.setClip(previousClip);
        }
    }

    private void drawHud(Graphics2D g) {
        g.setColor(new Color(32, 43, 60));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 16));
        g.drawString("TOWER HOP  |  HEIGHT: " + (session == null ? 0 : session.getFloorsClimbed()), 18, 34);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString(message, 18, 78);
        if (session != null) {
            Difficulty difficulty = session.getSettings().getDifficulty();
            g.drawString("Difficulty: " + difficulty.getDisplayName()
                    + "    Start speed: " + difficulty.getStartingSpeedLabel()
                    + "    Climb boost: +" + session.getSpeedBoostPercent() + "%"
                    + (session.isTimeSlowed()
                    ? "    Slow time: " + session.getSlowSecondsRemaining() + "s" : ""), 18, 100);
            if (!awaitingStart) {
                drawStopwatchSkill(g);
            }
        }
        g.setColor(new Color(74, 90, 108));
        g.setStroke(new BasicStroke(1f));
        g.drawRect(8, 8, getWidth() - 17, getHeight() - 17);
    }

    private void drawPlayer(Graphics2D g) {
        boolean facingLeft = session != null && session.isFacingLeft();
        PlayerAnimator animator = playerAnimators.get(playerSkin);
        boolean airborne = session != null && (session.isJumping() || session.isFalling());
        boolean running = session != null && session.isRunning() && !session.isPaused() && !paused;
        BufferedImage sprite = animator.frame(airborne, running, System.nanoTime());
        int scale = playerScale();
        int bottomPadding = animator.getBottomTransparentRows();
        int x = session == null ? 320 : worldToScreenX(session.getPlayerX());
        double worldY = session == null ? 0.0 : session.getPlayerWorldY();
        int width = sprite.getWidth() * scale;
        int height = sprite.getHeight() * scale;
        // Keep the transparent rows below the paws aligned with the platform.
        int y = worldToScreenY(worldY) - height + bottomPadding * scale;
        if (facingLeft && playerSkin != PlayerSkin.SHAYNE) {
            g.drawImage(sprite, x + width, y, x, y + height,
                    0, 0, sprite.getWidth(), sprite.getHeight(), null);
        } else {
            g.drawImage(sprite, x, y, x + width, y + height,
                    0, 0, sprite.getWidth(), sprite.getHeight(), null);
        }
    }

    private int playerScale() {
        return Math.max(1, PLAYER_RENDER_HEIGHT / PixelArt.player(playerSkin).getHeight());
    }

    private int worldToScreenX(double worldX) {
        if (session == null) {
            return (int) Math.round(worldX);
        }
        BufferedImage sprite = PixelArt.player(playerSkin);
        double playerWidth = sprite.getWidth() * playerScale();
        double targetPlayerX = (getWidth() - playerWidth) / 2.0;
        return (int) Math.round(targetPlayerX + worldX - session.getPlayerX());
    }

    private int worldToScreenY(double worldY) {
        if (session == null) {
            return (int) Math.round(getHeight() - 35.0 - worldY);
        }
        BufferedImage sprite = PixelArt.player(playerSkin);
        int scale = playerScale();
        double spriteHeight = sprite.getHeight() * scale;
        double bottomPadding = playerAnimators.get(playerSkin).getBottomTransparentRows() * scale;
        double targetFootY = getHeight() / 2.0 + spriteHeight / 2.0 - bottomPadding;
        return (int) Math.round(targetFootY + session.getPlayerWorldY() - worldY);
    }

    private void drawStopwatchPickups(Graphics2D g) {
        if (session == null || !session.getSettings().isShowStopwatch()) {
            return;
        }
        for (StopwatchPickup pickup : session.getStopwatchPickups()) {
            int y = worldToScreenY(pickup.getPlatform().getY()) - PICKUP_DRAW_HEIGHT;
            int x = worldToScreenX(pickup.getX()) + (StopwatchPickup.HITBOX_WIDTH - PICKUP_DRAW_WIDTH) / 2;
            g.drawImage(stopwatchSprite, x, y,
                    PICKUP_DRAW_WIDTH, PICKUP_DRAW_HEIGHT, null);
        }
    }

    private void drawStopwatchSkill(Graphics2D g) {
        java.awt.Rectangle bounds = skillBounds();
        g.setColor(new Color(28, 42, 61, 228));
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        g.setColor(new Color(222, 237, 248));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);
        g.drawImage(stopwatchSprite, bounds.x + 8, bounds.y + 10, 36, 44, null);

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g.drawString("STOPWATCH " + session.getStopwatchCharges() + "/"
                + session.getMaxStopwatchCharges(), bounds.x + 48, bounds.y + 27);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        int cooldown = session.getStopwatchCooldownSecondsRemaining();
        String status;
        if (!session.getSettings().isShowStopwatch()) {
            status = "OFF IN SETTINGS";
        } else if (cooldown > 0) {
            status = "CD " + String.format("%02d:%02d", cooldown / 60, cooldown % 60);
        } else if (session.getStopwatchCharges() > 0) {
            status = "READY  [E]";
        } else {
            status = "FIND ONE";
        }
        g.drawString(status, bounds.x + 48, bounds.y + 48);
    }

    private java.awt.Rectangle skillBounds() {
        return new java.awt.Rectangle(getWidth() - 178, getHeight() - 80, 168, 68);
    }

    private void drawStartPrompt(Graphics2D g) {
        String label = "PRESS SPACEBAR TO START";
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 21));
        java.awt.FontMetrics metrics = g.getFontMetrics();
        int width = metrics.stringWidth(label) + 44;
        int height = 76;
        int x = (getWidth() - width) / 2;
        int y = Math.max(110, getHeight() / 5);

        g.setColor(new Color(12, 23, 38, 232));
        g.fillRect(x + 7, y, width - 14, height);
        g.fillRect(x, y + 7, width, height - 14);
        g.setColor(new Color(131, 190, 153));
        g.fillRect(x + 9, y + 8, width - 18, 4);
        g.setColor(new Color(224, 239, 235));
        g.drawRect(x + 4, y + 4, width - 9, height - 9);
        g.setColor(new Color(255, 222, 127));
        int textX = x + (width - metrics.stringWidth(label)) / 2;
        int textY = y + (height - metrics.getHeight()) / 2 + metrics.getAscent();
        g.drawString(label, textX, textY);
    }

    private void drawDifficultySelector(Graphics2D g) {
        java.awt.Rectangle first = difficultyBounds(0);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        String heading = "SELECT DIFFICULTY / START SPEED";
        java.awt.FontMetrics headingMetrics = g.getFontMetrics();
        g.setColor(new Color(32, 43, 60));
        g.drawString(heading, (getWidth() - headingMetrics.stringWidth(heading)) / 2,
                first.y - 30);
        String hint = "CLICK A LEVEL OR PRESS 1-5";
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        java.awt.FontMetrics hintMetrics = g.getFontMetrics();
        g.drawString(hint, (getWidth() - hintMetrics.stringWidth(hint)) / 2,
                first.y - 12);

        Difficulty[] difficulties = Difficulty.values();
        for (int index = 0; index < difficulties.length; index++) {
            Difficulty difficulty = difficulties[index];
            java.awt.Rectangle bounds = difficultyBounds(index);
            boolean selected = pendingSettings != null
                    && pendingSettings.getDifficulty() == difficulty;
            g.setColor(new Color(11, 22, 36));
            g.fillRect(bounds.x + 4, bounds.y + 4, bounds.width, bounds.height);
            g.setColor(selected ? new Color(78, 133, 98) : new Color(43, 62, 82));
            g.fillRect(bounds.x + 4, bounds.y, bounds.width - 8, bounds.height);
            g.fillRect(bounds.x, bounds.y + 4, bounds.width, bounds.height - 8);
            g.setColor(selected ? new Color(255, 222, 127) : new Color(215, 231, 237));
            g.drawRect(bounds.x + 3, bounds.y + 3, bounds.width - 7, bounds.height - 7);

            g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
            java.awt.FontMetrics nameMetrics = g.getFontMetrics();
            g.drawString(difficulty.getDisplayName().toUpperCase(),
                    bounds.x + (bounds.width - nameMetrics.stringWidth(
                            difficulty.getDisplayName().toUpperCase())) / 2,
                    bounds.y + 23);
            g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            java.awt.FontMetrics speedMetrics = g.getFontMetrics();
            String speedLabel = difficulty.getStartingSpeedLabel();
            g.drawString(speedLabel,
                    bounds.x + (bounds.width - speedMetrics.stringWidth(speedLabel)) / 2,
                    bounds.y + 43);
        }
    }

    private java.awt.Rectangle difficultyBounds(int index) {
        int count = Difficulty.values().length;
        int gap = 10;
        int width = Math.min(136, (getWidth() - 48 - gap * (count - 1)) / count);
        int totalWidth = width * count + gap * (count - 1);
        int startX = (getWidth() - totalWidth) / 2;
        int y = Math.max(190, getHeight() - 90);
        return new java.awt.Rectangle(startX + index * (width + gap), y, width, 58);
    }

    private void selectDifficulty(Difficulty difficulty) {
        if (pendingSettings == null || pendingSettings.getDifficulty() == difficulty) {
            return;
        }
        pendingSettings = pendingSettings.withDifficulty(difficulty);
        onDifficultySelected.accept(difficulty);
        prepareForStart(pendingSettings);
    }

    private void drawPauseOverlay(Graphics2D g) {
        g.setColor(new Color(9, 18, 31, 204));
        g.fillRect(0, 0, getWidth(), getHeight());

        int panelWidth = Math.min(600, getWidth() - 40);
        int panelHeight = 230;
        int panelX = (getWidth() - panelWidth) / 2;
        int panelY = (getHeight() - panelHeight) / 2;
        g.setColor(new Color(16, 28, 43));
        g.fillRect(panelX + 8, panelY, panelWidth - 16, panelHeight);
        g.fillRect(panelX, panelY + 8, panelWidth, panelHeight - 16);
        g.setColor(new Color(102, 155, 128));
        g.fillRect(panelX + 14, panelY + 13, panelWidth - 28, 5);
        g.setColor(new Color(219, 234, 231));
        g.drawRect(panelX + 5, panelY + 5, panelWidth - 11, panelHeight - 11);

        String title = "PAUSED";
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        java.awt.FontMetrics metrics = g.getFontMetrics();
        g.setColor(new Color(255, 222, 127));
        g.drawString(title, (getWidth() - metrics.stringWidth(title)) / 2, panelY + 72);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        metrics = g.getFontMetrics();
        String hint = "ESC TO RESUME";
        g.setColor(new Color(212, 226, 234));
        g.drawString(hint, (getWidth() - metrics.stringWidth(hint)) / 2, panelY + 103);

        drawPauseButton(g, pauseButtonBounds(0), "RESUME", true);
        drawPauseButton(g, pauseButtonBounds(1), "SETTING", false);
        drawPauseButton(g, pauseButtonBounds(2), "QUIT", false);
    }

    private void drawPauseButton(Graphics2D g, java.awt.Rectangle bounds,
            String label, boolean primary) {
        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;
        g.setColor(new Color(8, 15, 25));
        g.fillRect(x + 5, y + 5, width, height);
        g.setColor(primary ? new Color(75, 133, 98) : new Color(58, 78, 99));
        g.fillRect(x + 5, y, width - 10, height);
        g.fillRect(x, y + 5, width, height - 10);
        g.setColor(new Color(220, 237, 230));
        g.drawRect(x + 5, y + 4, width - 11, height - 9);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        java.awt.FontMetrics metrics = g.getFontMetrics();
        g.setColor(Color.WHITE);
        g.drawString(label, x + (width - metrics.stringWidth(label)) / 2,
                y + (height - metrics.getHeight()) / 2 + metrics.getAscent());
    }

    private java.awt.Rectangle pauseButtonBounds(int index) {
        int width = Math.min(150, (getWidth() - 80) / 3);
        int gap = 18;
        int totalWidth = width * 3 + gap * 2;
        int startX = (getWidth() - totalWidth) / 2;
        int y = (getHeight() - 230) / 2 + 140;
        return new java.awt.Rectangle(startX + index * (width + gap), y, width, 52);
    }

    private void handlePauseMenuClick(java.awt.Point point) {
        if (pauseButtonBounds(0).contains(point)) {
            playMenuClick();
            resumeGame();
        } else if (pauseButtonBounds(1).contains(point)) {
            playMenuClick();
            onPauseSettings.run();
        } else if (pauseButtonBounds(2).contains(point)) {
            playMenuClick();
            onQuitToMenu.run();
        }
    }

    private void playMenuClick() {
        clickSound.play(soundEnabled.getAsBoolean());
    }

    private void pauseGame() {
        paused = true;
        if (session != null) {
            session.pause();
        }
        repaint();
    }

    private void resumeGame() {
        paused = false;
        if (session != null) {
            session.resume();
        }
        lastUpdateNanos = System.nanoTime();
        requestFocusInWindow();
        repaint();
    }

    private void activateStopwatch() {
        if (session.useStopwatch()) {
            stopwatchSound.play(session.getSettings().isSoundEnabled());
            message = "Stopwatch active: running speed is halved for 3 seconds. Cooldown: 30 seconds.";
        } else if (session.getStopwatchCharges() == 0) {
            message = "No stopwatches. Look for a rare pickup on a platform.";
        } else {
            message = "Stopwatch is recharging for "
                    + session.getStopwatchCooldownSecondsRemaining() + " seconds.";
        }
    }

    private boolean requestJump() {
        boolean jumped = session.requestJump();
        if (jumped) {
            jumpSound.play(session.getSettings().isSoundEnabled());
        }
        return jumped;
    }

    private void startPreparedRun() {
        if (session == null || !session.isRunning()) {
            beginRun(pendingSettings);
            return;
        }
        session.resume();
        awaitingStart = false;
        paused = false;
        gameOverShown = false;
        lastUpdateNanos = System.nanoTime();
        message = "Auto-running. Some platforms move side to side. Space jumps; Esc pauses.";
        onRunStarted.run();
        requestFocusInWindow();
        repaint();
    }

    private void updateGame() {
        long now = System.nanoTime();
        double elapsedSeconds = Math.min((now - lastUpdateNanos) / 1_000_000_000.0, 0.05);
        lastUpdateNanos = now;
        if (session != null && session.isRunning() && !paused) {
            int previousCharges = session.getStopwatchCharges();
            boolean died = session.update(elapsedSeconds);
            if (session.getStopwatchCharges() > previousCharges) {
                message = "Stopwatch collected. Press E or click its icon to slow time.";
            }
            if (died) {
                message = "Game over. Add your player name to save the score.";
                if (!gameOverShown) {
                    gameOverShown = true;
                    gameOverSound.play(session.getSettings().isSoundEnabled());
                    onGameOver.accept(session.getFloorsClimbed());
                }
            }
        }
        repaint();
    }
}
