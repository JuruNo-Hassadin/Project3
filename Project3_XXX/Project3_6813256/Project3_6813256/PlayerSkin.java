/*
6813256 sornchai soontranon
6813259 hassadin kheawkhum 
6813255 Warisa Inthararak 
6813104 Shayne Chawengchot
*/

package Project3_6813256;

/** Player characters that can be selected in the settings dialog. */
public enum PlayerSkin {
    RUNNER("Runner"),
    DOG("Dog"),
    CAT("Cat"),
    SHAYNE("Shayne");

    private final String displayName;

    PlayerSkin(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
