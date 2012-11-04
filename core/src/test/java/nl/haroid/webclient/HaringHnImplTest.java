package nl.haroid.webclient;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.impl.SimpleLogger;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ruud de Jong
 */
public class HaringHnImplTest {

    @Before
    public void setLogging() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    @Test
    public void connectTest() throws IOException {
        Properties authenticationProps = new Properties();
        authenticationProps.load(this.getClass().getResourceAsStream("/HaringHnImplTest.properties"));
        String username = authenticationProps.getProperty("username");
        String password = authenticationProps.getProperty("password");

        HaringHnImpl haring = new HaringHnImpl();
        String response = haring.start(username, password);
        Assert.assertNotNull(response);

    }
}
