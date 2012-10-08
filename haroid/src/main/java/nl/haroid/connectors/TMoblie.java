package nl.haroid.connectors;

import android.util.Log;
import nl.haroid.Utils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dave Sarpong
 */
public final class TMoblie {
    private static final String LOG_TAG = "TMobile";

    private final String host = "www.t-mobile.nl";
    private final String schemeName = "https://";
    private final String relativeUrlStart = "/my_t-mobile/htdocs/page/my_tmobile/login/login.aspx";
    private final String relativeUrlVerbruik = "/My_T-mobile/htdocs/page/calling/status/callstatusview.aspx";
    private final String userAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.57 Safari/537.1";
    private String username;
    private String password;
    private TMobileDisplay tMobileDisplay;


    public String start(String username, String password) {
        this.username = username;
        this.password = password;
        Log.i(LOG_TAG, this.username);
        Log.i(LOG_TAG, this.password);
        try {
            login();
        } catch (IOException e) {
            return "Helaas niet gevonden.";
        }
        String display = Utils.deleteWhitespace(tMobileDisplay.getDisplayMinutes());
        String tegoed = Utils.remove(display, "Min/SMS");
        Log.i(LOG_TAG, tegoed);
        return tegoed;
    }

    private void login() throws IOException {
        Map<String, String> sessionInfo = new HashMap<String, String>();
        Connection.Response con = Jsoup.connect(schemeName + host + relativeUrlStart).userAgent(userAgent).method(Connection.Method.GET).execute();
        sessionInfo.putAll(con.cookies());

        Map<String, String> inputs = getInputParameters(con.parse());
        if (!inputs.isEmpty()){

            Connection.Response res = Jsoup.connect(schemeName + host + relativeUrlStart).timeout(10000).userAgent(userAgent).cookies(sessionInfo).data("__EVENTTARGET", inputs.get("LoginButton")).data("__EVENTARGUMENT", "").data(inputs.get("viewID"), inputs.get("view")).data("__EVENTVALIDATION", inputs.get("event")).data(inputs.get("username"), username).data(inputs.get("password"), password).method(Connection.Method.POST).execute();
            sessionInfo.putAll(res.cookies());

            Connection.Response overview = Jsoup.connect(schemeName + host + relativeUrlVerbruik).userAgent(userAgent).cookies(sessionInfo).execute();
            Document loggedIn = overview.parse();

            this.tMobileDisplay = new TMobileDisplay(loggedIn);
            Log.i(LOG_TAG, this.tMobileDisplay.getDisplayMinutes());
            Log.i(LOG_TAG, this.tMobileDisplay.getDisplayVerbruik().get("used"));
        }
    }

    private Map<String, String> getInputParameters(Document tmobpage) throws IOException {
        Map<String, String> inputParameters = new HashMap<String, String>();
        Elements inputs = tmobpage.getElementsByTag("input");
        for (Element element : inputs){
            if (element.attr("name").contains("EVENTVALIDATION")){
                inputParameters.put("event", element.attr("value"));
            }

            if (element.attr("name").contains("VIEWSTATE")){
                inputParameters.put("viewID", element.attr("id"));
                inputParameters.put("view", element.attr("value"));
            }

            if (element.attr("name").contains("username")){
                inputParameters.put("username", element.attr("name"));
            }

            if (element.attr("name").contains("password")){
                inputParameters.put("password", element.attr("name"));
            }

            if (element.attr("name").contains("Login")){
                inputParameters.put("LoginButton", element.attr("name"));
            }
        }
        return inputParameters;
    }

}