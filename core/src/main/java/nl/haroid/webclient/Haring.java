package nl.haroid.webclient;

/**
 * @author Ruud de Jong
 */
public interface Haring {

    /**
     * Bezoek de website en vind het tegoed.
     *
     * @param username de username.
     * @param password het wachtwoord.
     * @return het tegoed of een foutmelding.
     */
    String start(String username, String password);
}
