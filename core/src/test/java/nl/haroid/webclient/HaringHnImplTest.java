package nl.haroid.webclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ruud de Jong
 */
@Ignore
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

    @Test
    public void ongeldigeLogin() throws IOException {
        HaringHnImpl haring = new HaringHnImpl();
        String response = haring.start("dummy", "dummy");
        Assert.assertNull(response);
    }
}
