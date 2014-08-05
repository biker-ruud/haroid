package nl.haroid.common;

/**
 * @author Ruud de Jong
 */
public enum Theme {
    DARK("Donker"),
    LIGHT("Licht");

    private final String displayName;

    private Theme(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
