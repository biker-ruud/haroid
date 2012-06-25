package nl.haroid;

/**
 * @author Ruud de Jong
 */
public interface TegoedConsumer {

    /**
     * To set the 'tegoed' async.
     * @param tegoed
     */
    void setTegoed(int tegoed);

    /**
     * To set a problem while fetching data.
     * @param problem
     */
    void setProblem(String problem);
}
