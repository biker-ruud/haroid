package nl.haroid.webclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Ruud de Jong
 */
public class JavascriptHaringTest {

    @Before
    public void setLogging() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }

    @Test
    public void test() throws IOException {
        Properties authenticationProps = new Properties();
        authenticationProps.load(this.getClass().getResourceAsStream("/JavascriptHaringTest.properties"));
        String username = authenticationProps.getProperty("username");
        String password = authenticationProps.getProperty("password");

        JavaScriptHaring haring = new JavaScriptHaring();
        String response = haring.start(username, password);
        Assert.assertNotNull(response);
    }
}
