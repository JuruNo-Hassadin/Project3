/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/
package Project3_6813256;

/** Difficulty presets used to tune movement and platform generation. */
public enum Difficulty {
    PRACTICE("Practice", -40, 0.72),
    EASY("Easy", -20, 0.86),
    NORMAL("Normal", 0, 1.00),
    HARD("Hard", 50, 1.12),
    EXPERT("Expert", 100, 1.24);

    private final String displayName;
    private final int startingSpeedPercent;
    private final double gapScale;

    Difficulty(String displayName, int startingSpeedPercent, double gapScale) {
        this.displayName = displayName;
        this.startingSpeedPercent = startingSpeedPercent;
        this.gapScale = gapScale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getSpeedScale() {
        return 1.0 + startingSpeedPercent / 100.0;
    }

    public int getStartingSpeedPercent() {
        return startingSpeedPercent;
    }

    public String getStartingSpeedLabel() {
        return startingSpeedPercent > 0
                ? "+" + startingSpeedPercent + "%" : startingSpeedPercent + "%";
    }

    public double getGapScale() {
        return gapScale;
    }

    @Override
    public String toString() {
        return displayName + " (" + getStartingSpeedLabel() + ")";
    }
}
