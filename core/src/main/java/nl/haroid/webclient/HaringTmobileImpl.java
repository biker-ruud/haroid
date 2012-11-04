package nl.haroid.webclient;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author Dave Sarpong
 */
public final class HaringTmobileImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaringTmobileImpl.class);

    private static final String HOST = "www.t-mobile.nl";
    private static final String RELATIVE_URL_START = "/my_t-mobile/htdocs/page/my_tmobile/login/login.aspx";
    private static final String RELATIVE_URL_VERBRUIK = "/my_t-mobile/htdocs/page/calling/status/callstatusview.aspx";

    private String username;
    private String password;

    public String start(String username, String password) {
        this.username = username;
        this.password = password;

        HttpsSession session = null;
        try {
            session = new HttpsSession(new URL(HttpsSession.PROTOCOL + HOST));
            InputStream inputStream = session.connect(new URL(HttpsSession.PROTOCOL + HOST + RELATIVE_URL_START));
            if (inputStream != null) {
                login(session, inputStream);
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
        } catch (URISyntaxException e) {
            LOGGER.error("URL invalid: ", e);
        } finally {
            if (session != null) {
                session.disconnect();
            }
        }
        return "niet gevonden";
    }

    private String haalVerbruikGegevensOp(HttpsSession session) throws IOException {
        String tegoedIndicator = "minuten";
        InputStream inputStream = session.get(new URL(HttpsSession.PROTOCOL + HOST + RELATIVE_URL_VERBRUIK));
        String body = Utils.toString(inputStream);
        LOGGER.info("Response body size: " + body.length() + " bytes.");
        LOGGER.info("Response body: " + body);
        inputStream.close();

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
        LOGGER.info("Gevonden tegoed: " + tegoed);
        return tegoed;

    }

    private boolean login(HttpsSession session, InputStream inputStream) throws IOException, URISyntaxException {
        String body = Utils.toString(inputStream);
        inputStream.close();
        FormParserUtil.Form form = FormParserUtil.parseForm(body);

        if (form == null) {
            LOGGER.info("Kan het inlogscherm niet vinden");
            LOGGER.info(body);
            return false;
        }
        LOGGER.info("Form:");
        LOGGER.info("Form action: " + form.action);
        LOGGER.info("Form inputs: " + form.inputList.size());
        FormParserUtil.Input loginInput = FormParserUtil.getLoginInput(form);
        FormParserUtil.Input passwordInput = FormParserUtil.getPasswordInput(form);
        if (loginInput != null && passwordInput != null) {
            loginInput.value = username;
            passwordInput.value = password;
            FormParserUtil.postForm(session, form);
            return true;
        } else {
            LOGGER.info("Kan login en password velden NIET vinden op het login scherm.");
            LOGGER.info(body);
            return false;
        }
    }
}
