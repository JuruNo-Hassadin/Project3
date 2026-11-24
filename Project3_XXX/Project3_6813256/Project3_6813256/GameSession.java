/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/** Run state, movement, jumping, and platform collision for one play session. */
public final class GameSession {
    private static final double PLAYER_WIDTH = 64.0;
    private static final double BASE_RUN_SPEED = 220.0;
    private static final double GRAVITY = 1450.0;
    private static final double FALL_GRAVITY_MULTIPLIER = 1.30;
    private static final double JUMP_VELOCITY = 600.0;
    private static final int START_PLATFORM_X = 200;
    private static final int START_PLATFORM_Y = 275;
    private static final int START_PLATFORM_WIDTH = 700;
    private static final double START_PLAYER_X = 518.0;
    private static final int FLOORS_PER_SPEED_STEP = 10;
    private static final int MAX_SPEED_STEPS = 10;
    private static final int SPEED_INCREASE_PERCENT_PER_STEP = 10;
    private static final double FALL_DEATH_DISTANCE = 220.0;
    private static final double PLATFORM_LOOKAHEAD = 650.0;
    private static final int MAX_STOPWATCH_CHARGES = 3;
    private static final int MAX_ACTIVE_PICKUPS = 2;
    private static final int PICKUP_HITBOX_WIDTH = StopwatchPickup.HITBOX_WIDTH;
    private static final long STOPWATCH_EFFECT_MILLIS = 3_000L;
    private static final long STOPWATCH_COOLDOWN_MILLIS = 30_000L;

    private final GameSettings settings;
    private final List<Platform> platforms = new ArrayList<>();
    private final List<StopwatchPickup> stopwatchPickups = new ArrayList<>();
    private final PlatformGenerator platformGenerator = new PlatformGenerator();
    private final Random random = new Random();

    private long startedAtMillis;
    private long slowUntilMillis;
    private long stopwatchCooldownUntilMillis;
    private int floorsClimbed;
    private int stopwatchCharges;
    private double playerX;
    private double playerWorldY;
    private double verticalVelocity;
    private Platform currentPlatform;
    private boolean facingLeft;
    private boolean jumping;
    private boolean falling;
    private boolean running;
    private boolean paused;
    private long pauseStartedAtMillis;
    private long totalPausedMillis;
    private long coyoteUntilMillis;
    private boolean fell;
    private boolean resultRecorded;
    private LeaderboardEntry resultEntry;

    public GameSession(GameSettings settings) {
        this.settings = settings;
    }

    public void start() {
        startedAtMillis = System.currentTimeMillis();
        slowUntilMillis = 0L;
        stopwatchCooldownUntilMillis = 0L;
        floorsClimbed = 0;
        stopwatchCharges = 0;
        playerX = START_PLAYER_X;
        playerWorldY = START_PLATFORM_Y;
        verticalVelocity = 0.0;
        facingLeft = false;
        jumping = false;
        falling = false;
        fell = false;
        resultRecorded = false;
        resultEntry = null;
        running = true;
        paused = false;
        pauseStartedAtMillis = 0L;
        totalPausedMillis = 0L;
        coyoteUntilMillis = 0L;

        platforms.clear();
        stopwatchPickups.clear();
        platformGenerator.reset();
        currentPlatform = new Platform(
                START_PLATFORM_X, START_PLATFORM_Y, START_PLATFORM_WIDTH);
        platforms.add(currentPlatform);
        ensurePlatformsAhead();
    }

    /** Updates automatic running, gravity, and landing checks. Returns true on death. */
    public boolean update(double elapsedSeconds) {
        if (!running || paused) {
            return false;
        }
        double delta = Math.max(0.0, Math.min(elapsedSeconds, 0.05));
        double simulationDelta = delta * (isTimeSlowed() ? 0.5 : 1.0);
        int previousCurrentPlatformX = currentPlatform.getX();
        for (Platform platform : platforms) {
            if (platform.isMoving()) {
                platform.update(simulationDelta);
            }
        }
        if (!jumping && !falling) {
            playerX += currentPlatform.getX() - previousCurrentPlatformX;
        }

        double speed = runSpeedAtFloor(floorsClimbed);
        playerX += (facingLeft ? -1.0 : 1.0) * speed * simulationDelta;

        double previousY = playerWorldY;
        if (jumping || falling) {
            double gravity = verticalVelocity < 0.0
                    ? GRAVITY * FALL_GRAVITY_MULTIPLIER : GRAVITY;
            verticalVelocity -= gravity * simulationDelta;
            playerWorldY += verticalVelocity * simulationDelta;
            if (verticalVelocity < 0.0) {
                Platform landing = findLanding(previousY, playerWorldY);
                if (landing != null) {
                    landOn(landing);
                } else if (jumping && playerWorldY < currentPlatform.getY()) {
                    // A missed jump becomes a real fall once the player drops
                    // below the platform they launched from.
                    jumping = false;
                    falling = true;
                    coyoteUntilMillis = 0L;
                }
            }
        } else {
            playerWorldY = currentPlatform.getY();
            boolean leavingLeftEdge = facingLeft
                    && playerX <= currentPlatform.getX() + 8.0;
            boolean leavingRightEdge = !facingLeft
                    && playerX + PLAYER_WIDTH
                            >= currentPlatform.getX() + currentPlatform.getWidth() - 8.0;
            if (leavingLeftEdge || leavingRightEdge) {
                falling = true;
                verticalVelocity = 0.0;
                coyoteUntilMillis = System.currentTimeMillis() + 120L;
            }
        }

        if (playerWorldY < currentPlatform.getY() - FALL_DEATH_DISTANCE) {
            running = false;
            fell = true;
            return true;
        }
        collectPickupIfTouched();
        stopwatchPickups.removeIf(pickup ->
                pickup.getPlatform().getY() < playerWorldY - FALL_DEATH_DISTANCE - 80.0);
        return false;
    }

    /** Reverses direction and starts a jump from the platform under the player. */
    public boolean requestJump() {
        if (!running || paused || jumping) {
            return false;
        }
        if (falling) {
            if (System.currentTimeMillis() > coyoteUntilMillis
                    || playerWorldY < currentPlatform.getY() - 28.0) {
                return false;
            }
            falling = false;
            coyoteUntilMillis = 0L;
        }
        facingLeft = !facingLeft;
        jumping = true;
        verticalVelocity = JUMP_VELOCITY;
        return true;
    }

    public boolean useStopwatch() {
        long now = gameClockNowMillis();
        if (!running || paused || stopwatchCharges == 0 || now < stopwatchCooldownUntilMillis) {
            return false;
        }
        stopwatchCharges--;
        slowUntilMillis = now + STOPWATCH_EFFECT_MILLIS;
        stopwatchCooldownUntilMillis = now + STOPWATCH_COOLDOWN_MILLIS;
        return true;
    }

    public boolean isTimeSlowed() {
        return gameClockNowMillis() < slowUntilMillis;
    }

    public int getSlowSecondsRemaining() {
        long remaining = slowUntilMillis - gameClockNowMillis();
        return remaining <= 0 ? 0 : (int) Math.ceil(remaining / 1000.0);
    }

    public int getStopwatchCooldownSecondsRemaining() {
        long remaining = stopwatchCooldownUntilMillis - gameClockNowMillis();
        return remaining <= 0 ? 0 : (int) Math.ceil(remaining / 1000.0);
    }

    public void pause() {
        if (running && !paused) {
            paused = true;
            pauseStartedAtMillis = System.currentTimeMillis();
        }
    }

    public void resume() {
        if (!paused) {
            return;
        }
        long pauseDuration = Math.max(0L, System.currentTimeMillis() - pauseStartedAtMillis);
        totalPausedMillis += pauseDuration;
        if (slowUntilMillis > 0L) {
            slowUntilMillis += pauseDuration;
        }
        if (stopwatchCooldownUntilMillis > 0L) {
            stopwatchCooldownUntilMillis += pauseDuration;
        }
        if (coyoteUntilMillis > 0L) {
            coyoteUntilMillis += pauseDuration;
        }
        paused = false;
        pauseStartedAtMillis = 0L;
    }

    public LeaderboardEntry finish() {
        return finish(settings.getPlayerName());
    }

    public LeaderboardEntry finish(String playerName) {
        String cleanName = playerName == null || playerName.isBlank()
                ? settings.getPlayerName() : playerName.trim();
        if (startedAtMillis == 0L) {
            return null;
        }
        if (resultRecorded) {
            if (resultEntry != null) {
                resultEntry = resultEntry.withPlayerName(cleanName);
            }
            return resultEntry;
        }
        running = false;
        resultRecorded = true;
        long currentPause = paused
                ? Math.max(0L, System.currentTimeMillis() - pauseStartedAtMillis) : 0L;
        long duration = Math.max(0L,
                (System.currentTimeMillis() - startedAtMillis - totalPausedMillis - currentPause)
                        / 1000L);
        resultEntry = new LeaderboardEntry(LocalDateTime.now(), cleanName,
                settings.getDifficulty(), floorsClimbed, duration);
        return resultEntry;
    }

    public void discard() {
        running = false;
        resultRecorded = true;
        resultEntry = null;
    }

    private Platform findLanding(double previousY, double nextY) {
        Platform best = null;
        for (Platform platform : platforms) {
            double platformY = platform.getY();
            boolean crossedFromAbove = previousY >= platformY && nextY <= platformY;
            boolean overlapsHorizontally = playerX + PLAYER_WIDTH > platform.getX()
                    && playerX < platform.getX() + platform.getWidth();
            if (crossedFromAbove && overlapsHorizontally
                    && (best == null || platform.getY() > best.getY())) {
                best = platform;
            }
        }
        return best;
    }

    private void landOn(Platform platform) {
        if (platform.getY() > currentPlatform.getY()) {
            floorsClimbed++;
        }
        currentPlatform = platform;
        playerWorldY = platform.getY();
        verticalVelocity = 0.0;
        jumping = false;
        falling = false;
        coyoteUntilMillis = 0L;
        ensurePlatformsAhead();
        platforms.removeIf(old -> old != currentPlatform
                && old.getY() < playerWorldY - FALL_DEATH_DISTANCE - 80.0);
        stopwatchPickups.removeIf(pickup ->
                pickup.getPlatform().getY() < playerWorldY - FALL_DEATH_DISTANCE - 80.0);
    }

    private void ensurePlatformsAhead() {
        Platform highest = currentPlatform;
        int plannedLandings = 0;
        for (Platform platform : platforms) {
            if (platform.getY() > highest.getY()) {
                highest = platform;
            }
            if (platform.getY() > currentPlatform.getY()) {
                plannedLandings++;
            }
        }
        boolean plannedFacingLeft = facingLeft;
        for (int i = 0; i < plannedLandings; i++) {
            plannedFacingLeft = !plannedFacingLeft;
        }
        while (highest.getY() < playerWorldY + PLATFORM_LOOKAHEAD) {
            Platform next = platformGenerator.generateAbove(
                    highest, settings.getDifficulty(), plannedFacingLeft,
                    runSpeedAtFloor(floorsClimbed + plannedLandings), random);
            platforms.add(next);
            maybeSpawnStopwatch(next);
            highest = next;
            plannedFacingLeft = !plannedFacingLeft;
            plannedLandings++;
        }
    }

    private double runSpeedAtFloor(int floor) {
        int speedSteps = Math.min(Math.max(0, floor) / FLOORS_PER_SPEED_STEP,
                MAX_SPEED_STEPS);
        double progression = 1.0 + speedSteps * SPEED_INCREASE_PERCENT_PER_STEP / 100.0;
        return BASE_RUN_SPEED * settings.getDifficulty().getSpeedScale() * progression;
    }

    private long gameClockNowMillis() {
        return paused ? pauseStartedAtMillis : System.currentTimeMillis();
    }

    private void maybeSpawnStopwatch(Platform platform) {
        if (!settings.isShowStopwatch()
                || stopwatchCharges >= MAX_STOPWATCH_CHARGES
                || stopwatchPickups.size() >= MAX_ACTIVE_PICKUPS
                || random.nextInt(16) != 0) {
            return;
        }
        int padding = 24;
        int maxOffset = Math.max(0,
                platform.getWidth() - padding * 2 - PICKUP_HITBOX_WIDTH);
        int x = platform.getX() + padding + random.nextInt(maxOffset + 1);
        stopwatchPickups.add(new StopwatchPickup(platform, x));
    }

    private void collectPickupIfTouched() {
        if (!running || jumping || falling || stopwatchCharges >= MAX_STOPWATCH_CHARGES) {
            return;
        }
        Iterator<StopwatchPickup> iterator = stopwatchPickups.iterator();
        while (iterator.hasNext()) {
            StopwatchPickup pickup = iterator.next();
            if (pickup.getPlatform() == currentPlatform
                    && playerX + PLAYER_WIDTH > pickup.getX()
                    && playerX < pickup.getX() + PICKUP_HITBOX_WIDTH) {
                stopwatchCharges++;
                iterator.remove();
                break;
            }
        }
    }


    public List<Platform> getPlatforms() {
        return Collections.unmodifiableList(platforms);
    }

    public GameSettings getSettings() { return settings; }
    public int getFloorsClimbed() { return floorsClimbed; }
    public int getStopwatchCharges() { return stopwatchCharges; }
    public int getMaxStopwatchCharges() { return MAX_STOPWATCH_CHARGES; }
    public int getSpeedBoostPercent() {
        int speedSteps = Math.min(floorsClimbed / FLOORS_PER_SPEED_STEP, MAX_SPEED_STEPS);
        return speedSteps * SPEED_INCREASE_PERCENT_PER_STEP;
    }
    public List<StopwatchPickup> getStopwatchPickups() {
        return Collections.unmodifiableList(stopwatchPickups);
    }
    public boolean isFacingLeft() { return facingLeft; }
    public boolean isRunning() { return running; }
    public boolean isPaused() { return paused; }
    public boolean hasFallen() { return fell; }
    public boolean isJumping() { return jumping; }
    public boolean isFalling() { return falling; }
    public double getPlayerX() { return playerX; }
    public double getPlayerWorldY() { return playerWorldY; }
}
