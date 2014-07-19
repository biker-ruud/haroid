package nl.haroid.common;

/**
 * @author Xilv3r
 */
public enum Provider {
    // TODO (Xilv3r): add string variable so menu options look cleaner
    HOLLANDS_NIEUWE("Hollands Nieuwe"),
    HOLLANDS_NIEUWE_ZAKELIJK("Hollands Nieuwe Zakelijk"),
    T_MOBILE("T-Mobile");

    private final String displayName;

    private Provider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }
}
