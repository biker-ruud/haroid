package nl.haroid.webclient;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Dave Sarpong
 */
public class HaringTmobileImplTest {

    @Test
    public void connectTest() {
        HaringTmobileImpl haring = new HaringTmobileImpl();
        String response = haring.start("", "");
        Assert.assertNotNull(response);

    }
}
