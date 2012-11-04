package nl.haroid.webclient;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Dave Sarpong
 */
public class HaringTmobileImplTest {

    @Test
    public void connectTest() throws IOException {
        Properties authenticationProps = new Properties();
        authenticationProps.load(this.getClass().getResourceAsStream("/HaringTmobileImplTest.properties"));
        String username = authenticationProps.getProperty("username");
        String password = authenticationProps.getProperty("password");

        HaringTmobileImpl haring = new HaringTmobileImpl();
        String response = haring.start(username, password);
        Assert.assertNotNull(response);

    }
}
