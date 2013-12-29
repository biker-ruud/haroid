package nl.haroid.webclient;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author R de Jong
 */
public final class HaringTmobileImpl extends AbstractHaring {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaringTmobileImpl.class);

    private static final String HOST = "www.t-mobile.nl";
    private static final String RELATIVE_URL_START = "/my_t-mobile/htdocs/page/my_tmobile/login/login.aspx";
    private static final String RELATIVE_URL_VERBRUIK = "/my_t-mobile/htdocs/page/calling/status/callstatusview.aspx";

    @Override
    protected String getHost() {
        return HOST;
    }

    @Override
    protected String getRelativeStartUrl() {
        return RELATIVE_URL_START;
    }

    @Override
    protected String getRelativeVerbruikUrl() {
        return RELATIVE_URL_VERBRUIK;
    }

    @Override
    protected String vindTegoed(String body) {
        String tegoedIndicator = "Min/SMS";
        String[] tdList = Utils.substringsBetween(body, "<td ", "</td>");
        String tegoed = null;
        if (tdList != null) {
            for (String tdItem : tdList) {
                if (Utils.contains(tdItem, tegoedIndicator)) {
                    LOGGER.info("Gevonden tdItem: " + tdItem);
                    String unfilteredTdItem = Utils.substringBetween(tdItem, ">", tegoedIndicator);
                    tegoed = Utils.deleteWhitespace(unfilteredTdItem);
                }
            }
        }
        return tegoed;
    }

}
