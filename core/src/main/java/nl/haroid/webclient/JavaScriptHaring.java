package nl.haroid.webclient;

import com.google.gson.Gson;
import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public class JavaScriptHaring implements Haring {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaScriptHaring.class);
    private static final String HOST = "www.hollandsnieuwe.nl";
    private static final String RELATIVE_URL_START = "/login";
    private static final String RELATIVE_URL_PROFIEL = "/rest/v/1/client/mijn/profile/";
    private static final String RELATIVE_URL_VERBRUIK = "/myaccount/subscriptionPurchaseCurrentUsageDataFeed.jsp?index=0&status=ACTIVE&profileId=";

    private String username;
    private String password;

    @Override
    public String start(String username, String password) {
        this.username = username;
        this.password = password;

        HttpsSession session = null;
        try {
            session = new HttpsSession(new URL(HttpsSession.PROTOCOL + HOST));
            InputStream inputStream = session.connect(new URL(HttpsSession.PROTOCOL + HOST + RELATIVE_URL_START));
            if (inputStream != null) {
                if (!login(session, inputStream)) {
                    LOGGER.info("Login gefaald.");
                    return null;
                }
                String klantid = vindProfielId(session.getCookie(HttpsSession.JWT_COOKIE));
                String subscription = vindSubscriptionId(session, klantid);
                String tegoed = haalVerbruikGegevensOp(session);
                LOGGER.debug("Tegoed: " + tegoed);
                return tegoed;
            }
        } catch (MalformedURLException e) {
            LOGGER.error("URL invalid: ", e);
        } catch (ClassCastException e) {
            LOGGER.error("Class cast: ", e);
        } catch (SocketTimeoutException e) {
            LOGGER.warn("Timeout: ", e);
        } catch (IOException e) {
            LOGGER.error("IO error: ", e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
        return "niet gevonden";
    }

    private boolean login(HttpsSession session, InputStream inputStream) throws IOException {
        LOGGER.info("***************************************");
        LOGGER.info("**        INLOGGEN                   **");
        LOGGER.info("***************************************");
        String body = Utils.toString(inputStream);
        LOGGER.debug("Response body size: " + body.length() + " bytes.");
        LOGGER.trace("Body: " + body);
        inputStream.close();
        String jsonString = "{\"emailAddress\":\"" + username + "\",\"password\":\"" + password + "\",\"brandType\":\"CONSUMER\"}";
        session.post(new URL(HttpsSession.PROTOCOL + HOST + "/rest/auth/login"), jsonString);
        return (session.containsCookie(HttpsSession.JWT_COOKIE));
    }

    private String vindSubscriptionId(HttpsSession session, String klantid) throws IOException {
        LOGGER.info("***************************************");
        LOGGER.info("**        PROFIEL DETAILS OPHALEN    **");
        LOGGER.info("***************************************");
        InputStream inputStream = session.get(new URL(HttpsSession.PROTOCOL + HOST + RELATIVE_URL_PROFIEL + klantid));
        String body = Utils.toString(inputStream);
        LOGGER.info("Response body size: " + body.length() + " bytes.");
        LOGGER.trace("Body: " + body);
        inputStream.close();

        String subscriptionId = vindSubscription(body);
        LOGGER.info("Gevonden subscriptionId: " + subscriptionId);
        return subscriptionId;
    }

    private String haalVerbruikGegevensOp(HttpsSession session) throws IOException {
        LOGGER.info("***************************************");
        LOGGER.info("**        VERBRUIK OPHALEN           **");
        LOGGER.info("***************************************");
        InputStream inputStream = session.get(new URL(HttpsSession.PROTOCOL + HOST + RELATIVE_URL_VERBRUIK));
        String body = Utils.toString(inputStream);
        LOGGER.info("Response body size: " + body.length() + " bytes.");
        LOGGER.trace("Body: " + body);
        inputStream.close();

        String tegoed = vindTegoed(body);
        LOGGER.info("Gevonden tegoed: " + tegoed);
        return tegoed;
    }

    private String vindSubscription(String body) {
        String test = body;
        return test;
    }

    private String vindTegoed(String body) {
        String tegoedIndicator = "MB, minuten";
        String tegoedBedrag = Utils.substringBetween(body, "<span class=\"amount\">", "</span>");
        tegoedBedrag = Utils.deleteWhitespace(tegoedBedrag);
        String[] strongList = Utils.substringsBetween(body, "<strong>", "</strong>");
        String tegoed = null;
        if (strongList != null) {
            for (String strongItem : strongList) {
                if (Utils.contains(strongItem, tegoedIndicator)) {
                    String filterItem = Utils.deleteWhitespace(strongItem);
                    LOGGER.info("Gevonden strongItem na filter: " + filterItem);
                    tegoed = Utils.substringBefore(filterItem, Utils.deleteWhitespace(tegoedIndicator));
                }
            }
        }
        LOGGER.info("Gevonden tegoed bedrag: " + tegoedBedrag);
        return tegoed;
    }

    private String vindProfielId(String jsonWebToken) {
        int payloadStart = jsonWebToken.indexOf('.');
        if (payloadStart == -1) {
            return null;
        }
        int payloadEnd = jsonWebToken.indexOf('.', payloadStart + 1);
        if (payloadEnd == -1) {
            return null;
        }
        String encodedPayload = jsonWebToken.substring(payloadStart + 1, payloadEnd);
        String payload = new String(DatatypeConverter.parseBase64Binary(encodedPayload));
        if (!payload.endsWith("}")) {
            payload = payload + "}";
        }
        Gson gson = new Gson();
        JwtPayload jwtPayload = gson.fromJson(payload, JwtPayload.class);
        return jwtPayload.getProfileId();
    }

}
