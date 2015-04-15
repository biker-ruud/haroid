package nl.haroid.webclient;

import nl.haroid.common.Provider;

/**
 * @author Ruud de Jong
 */
public class HaringFactory {

    private static HaringFactory haringFactory = new HaringFactory();

    private HaringFactory() {
        // Utility class
    }

    public static HaringFactory getInstance() {
        return haringFactory;
    }

    public Haring getHaring(Provider provider) {
        switch (provider){
            case T_MOBILE:
                return new HaringTmobileImpl();
            case HOLLANDS_NIEUWE_ZAKELIJK:
                return new HaringHnZakelijkImpl();
            default:
                return new JavaScriptHaring();
        }

    }
}
