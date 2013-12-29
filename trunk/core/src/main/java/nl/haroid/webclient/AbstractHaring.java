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
 * @author Ruud de Jong
 */
public abstract class AbstractHaring implements Haring {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHaring.class);

    private String username;
    private String password;

    @Override
    public final String start(String username, String password) {
        this.username = username;
        this.password = password;

        HttpsSession session = null;
        try {
            session = new HttpsSession(new URL(HttpsSession.PROTOCOL + getHost()));
            InputStream inputStream = session.connect(new URL(HttpsSession.PROTOCOL + getHost() + getRelativeStartUrl()));
            if (inputStream != null) {
                if (!login(session, inputStream)) {
                    // Login gefaald.
                    return null;
                }
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

    protected abstract String getHost();

    protected abstract String getRelativeStartUrl();

    protected abstract String getRelativeVerbruikUrl();

    protected abstract String vindTegoed(String body);

    private boolean login(HttpsSession session, InputStream inputStream) throws IOException, URISyntaxException {
        LOGGER.info("***************************************");
        LOGGER.info("**        INLOGGEN                   **");
        LOGGER.info("***************************************");
        String body = Utils.toString(inputStream);
        LOGGER.debug("Response body size: " + body.length() + " bytes.");
        LOGGER.trace("Body: " + body);
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
            return FormParserUtil.postForm(session, form);
        } else {
            LOGGER.info("Kan login en password velden NIET vinden op het login scherm.");
            return false;
        }
    }

    private String haalVerbruikGegevensOp(HttpsSession session) throws IOException {
        LOGGER.info("***************************************");
        LOGGER.info("**        VERBRUIK OPHALEN           **");
        LOGGER.info("***************************************");
        InputStream inputStream = session.get(new URL(HttpsSession.PROTOCOL + getHost() + getRelativeVerbruikUrl()));
        String body = Utils.toString(inputStream);
        LOGGER.info("Response body size: " + body.length() + " bytes.");
        LOGGER.trace("Body: " + body);
        inputStream.close();

        String tegoed = vindTegoed(body);
        LOGGER.info("Gevonden tegoed: " + tegoed);
        return tegoed;
    }

}
