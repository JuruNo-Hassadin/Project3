/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

/** Immutable settings snapshot for one run. */
public final class GameSettings {
    private final String playerName;
    private final Difficulty difficulty;
    private final PlayerSkin playerSkin;
    private final boolean soundEnabled;
    private final boolean landingGuide;
    private final boolean showStopwatch;
    private final boolean reducedShake;
    private final int volume;
    private final boolean musicMuted;

    public GameSettings(String playerName, Difficulty difficulty,
            boolean soundEnabled, boolean landingGuide, boolean showStopwatch,
            boolean reducedShake, int volume) {
        this(playerName, difficulty, PlayerSkin.RUNNER, soundEnabled, landingGuide,
                showStopwatch, reducedShake, volume, false);
    }

    public GameSettings(String playerName, Difficulty difficulty, PlayerSkin playerSkin, // player skin
            boolean soundEnabled, boolean landingGuide, boolean showStopwatch,
            boolean reducedShake, int volume) {
        this(playerName, difficulty, playerSkin, soundEnabled, landingGuide,
                showStopwatch, reducedShake, volume, false);
    }

    public GameSettings(String playerName, Difficulty difficulty, PlayerSkin playerSkin,
            boolean soundEnabled, boolean landingGuide, boolean showStopwatch,
            boolean reducedShake, int volume, boolean musicMuted) {             // player skin and music mute
        String cleanedName = playerName == null ? "" : playerName.trim();
        this.playerName = cleanedName.isEmpty() ? "Player" : cleanedName;
        this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        this.playerSkin = playerSkin == null ? PlayerSkin.RUNNER : playerSkin;
        this.soundEnabled = soundEnabled;
        this.landingGuide = landingGuide;
        this.showStopwatch = showStopwatch;
        this.reducedShake = reducedShake;
        this.volume = Math.max(0, Math.min(100, volume));
        this.musicMuted = musicMuted;
    }

    public static GameSettings defaults() {
        return new GameSettings("Player", Difficulty.NORMAL, PlayerSkin.RUNNER,
                true, true, true, true, 65, false);
    }

    public String getPlayerName() { return playerName; }
    public Difficulty getDifficulty() { return difficulty; }
    public PlayerSkin getPlayerSkin() { return playerSkin; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isLandingGuide() { return landingGuide; }
    public boolean isShowStopwatch() { return showStopwatch; }
    public boolean isReducedShake() { return reducedShake; }
    public int getVolume() { return volume; }
    public boolean isMusicMuted() { return musicMuted; }

    public GameSettings withVolume(int newVolume) {
        return new GameSettings(playerName, difficulty, playerSkin, soundEnabled,
                landingGuide, showStopwatch, reducedShake, newVolume, musicMuted);
    }

    public GameSettings withDifficulty(Difficulty newDifficulty) {
        return new GameSettings(playerName, newDifficulty, playerSkin, soundEnabled,
                landingGuide, showStopwatch, reducedShake, volume, musicMuted);
    }

    public GameSettings withPlayerSkin(PlayerSkin newPlayerSkin) {
        return new GameSettings(playerName, difficulty, newPlayerSkin, soundEnabled,
                landingGuide, showStopwatch, reducedShake, volume, musicMuted);
    }
}
