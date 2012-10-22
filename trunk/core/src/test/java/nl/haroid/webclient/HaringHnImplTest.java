package nl.haroid.webclient;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.impl.SimpleLogger;

/**
 * @author Ruud de Jong
 */
public class HaringHnImplTest {

    @Before
    public void setLogging() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    @Test
    public void connectTest() {
        HaringHnImpl haring = new HaringHnImpl();
        String response = haring.start("", "");
        Assert.assertNotNull(response);

    }
}
