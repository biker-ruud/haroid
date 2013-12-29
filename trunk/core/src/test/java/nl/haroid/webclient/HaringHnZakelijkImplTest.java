package nl.haroid.webclient;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ruud de Jong
 */
public class HaringHnZakelijkImplTest {

    @Test
    public void connectTest() throws IOException {
        Properties authenticationProps = new Properties();
        authenticationProps.load(this.getClass().getResourceAsStream("/HaringHnZakelijkImplTest.properties"));
        String username = authenticationProps.getProperty("username");
        String password = authenticationProps.getProperty("password");

        HaringHnZakelijkImpl haring = new HaringHnZakelijkImpl();
        String response = haring.start(username, password);
        Assert.assertNotNull(response);
    }

    @Test
    public void ongeldigeLogin() throws IOException {
        HaringHnZakelijkImpl haring = new HaringHnZakelijkImpl();
        String response = haring.start("dummy", "dummy");
        Assert.assertNull(response);
    }
}
