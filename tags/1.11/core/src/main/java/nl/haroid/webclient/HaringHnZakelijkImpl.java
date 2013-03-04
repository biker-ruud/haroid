package nl.haroid.webclient;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ruud de Jong
 */
public class HaringHnZakelijkImpl extends AbstractHaring {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaringHnZakelijkImpl.class);
    private static final String HOST = "zakelijk.hollandsnieuwe.nl";
    private static final String RELATIVE_URL_START = "/mijn_hollandsnieuwe";
    private static final String RELATIVE_URL_VERBRUIK = "/myaccount/subscriptionPurchaseCurrentUsageDataFeed.jsp?index=0&status=ACTIVE&profileId=";

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
        String tegoedIndicator = "minuten";
        String tegoedBedrag = Utils.substringBetween(body, "<span class=\"usage\"><span class=\"amount\">", "</span>");
        String[] strongList = Utils.substringsBetween(body, "<strong>", "</strong>");
        String tegoed = null;
        if (strongList != null) {
            for (String strongItem : strongList) {
                if (Utils.contains(strongItem, tegoedIndicator)) {
                    LOGGER.debug("Gevonden strongItem: " + strongItem);
                    String filterItem = Utils.deleteWhitespace(strongItem);
                    tegoed = Utils.substringBefore(filterItem, tegoedIndicator);
                }
            }
        }
        LOGGER.info("Gevonden tegoed bedrag: " + tegoedBedrag);
        return tegoed;
    }
}
