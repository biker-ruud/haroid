package nl.haroid;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author Ruud de Jong
 */
public class HaringHnImplTest {

    @Test
    public void connectTest() {
        HaringHnImpl haring = new HaringHnImpl();
        String response = haring.start("", "");
        Assert.assertNotNull(response);

    }
}
