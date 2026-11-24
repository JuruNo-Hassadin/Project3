package Project3_6813256;

/** Immutable settings snapshot for one run. */
public final class GameSettings {
    private final String playerName;
    private final Difficulty difficulty;
    private final boolean soundEnabled;
    private final boolean highContrast;
    private final boolean landingGuide;
    private final boolean showStopwatch;
    private final boolean reducedShake;
    private final int volume;

    public GameSettings(String playerName, Difficulty difficulty,
            boolean soundEnabled, boolean highContrast, boolean landingGuide,
            boolean showStopwatch, boolean reducedShake, int volume) { // Constructor set bounch of constants
        String cleanedName = playerName == null ? "" : playerName.trim();
        this.playerName = cleanedName.isEmpty() ? "Player" : cleanedName;
        this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        this.soundEnabled = soundEnabled;
        this.highContrast = highContrast;
        this.landingGuide = landingGuide;
        this.showStopwatch = showStopwatch;
        this.reducedShake = reducedShake;
        this.volume = Math.max(0, Math.min(100, volume));
    }

    public static GameSettings defaults() {  // method defaults returns this class object
        return new GameSettings("Player", Difficulty.NORMAL,
                true, false, true, true, true, 65);
    }

    public String getPlayerName() { return playerName; }  // getMethods
    public Difficulty getDifficulty() { return difficulty; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isHighContrast() { return highContrast; }
    public boolean isLandingGuide() { return landingGuide; }
    public boolean isShowStopwatch() { return showStopwatch; }
    public boolean isReducedShake() { return reducedShake; }
    public int getVolume() { return volume; }

    public GameSettings withVolume(int newVolume) {   // setter valume but return whole object
        return new GameSettings(playerName, difficulty, soundEnabled, highContrast,
                landingGuide, showStopwatch, reducedShake, newVolume);
    }

    public GameSettings withDifficulty(Difficulty newDifficulty) {   // setter difficulty but return whole object
        return new GameSettings(playerName, newDifficulty, soundEnabled, highContrast,
                landingGuide, showStopwatch, reducedShake, volume);
    }
}
