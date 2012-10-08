package nl.haroid.connectors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dave Sarpong
 */
public final class TMobileDisplay {

    private Document mainDocument;

    public TMobileDisplay(Document document) {
        this.mainDocument = document;
        getDisplayMinutes();
        getDisplayVerbruik();
    }

    public String getDisplayMinutes() {
        String text = "";
        Elements elements = mainDocument.getElementsByTag("td");
        for (Element element : elements) {
            if (element.ownText().contains("Min")){
                text = element.ownText();
            }
        }

        return text;
    }

    public Map<String, String> getDisplayVerbruik() {
        Map<String, String> info = new HashMap<String, String>();
        Elements elements = mainDocument.getElementsByTag("span");
        for (Element element : elements) {
            if (element.attr("id").contains("dataUsage")){
                info.put("used", element.ownText());
            }

            if (element.attr("id").contains("remainingData")){
                info.put("remaining", element.ownText());
            }
        }
        return info;
    }
}
