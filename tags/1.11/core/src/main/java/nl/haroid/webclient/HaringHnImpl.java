package nl.haroid.webclient;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Ruud de Jong
 */
public final class HaringHnImpl extends AbstractHaring {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaringHnImpl.class);
    private static final String HOST = "www.hollandsnieuwe.nl";
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

//    public String start_oud(String username, String password) {
//        this.username = username;
//        this.password = password;
//
//        HttpsSession session = null;
//        try {
//            session = new HttpsSession(new URL(HttpsSession.PROTOCOL + HOST));
//            InputStream inputStream = session.connect(new URL(HttpsSession.PROTOCOL + HOST + RELATIVE_URL_START));
//            if (inputStream != null) {
//                login(session, inputStream);
//                String tegoed = haalVerbruikGegevensOp(session);
//                LOGGER.debug("Tegoed: " + tegoed);
//                return tegoed;
//            }
//        } catch (MalformedURLException e) {
//            LOGGER.error("URL invalid: ", e);
//        } catch (ClassCastException e) {
//            LOGGER.error("Class cast: ", e);
//        } catch (SocketTimeoutException e) {
//            LOGGER.warn("Timeout: ", e);
//        } catch (IOException e) {
//            LOGGER.error("IO error: ", e);
//        } catch (URISyntaxException e) {
//            LOGGER.error("URL invalid: ", e);
//        } finally {
//            if (session != null) {
//                session.disconnect();
//            }
//        }
//        return "niet gevonden";
//    }
//
//    private String haalVerbruikGegevensOp(HttpsSession session) throws IOException {
//        LOGGER.info("***************************************");
//        LOGGER.info("**        VERBRUIK OPHALEN           **");
//        LOGGER.info("***************************************");
//        String tegoedIndicator = "minuten";
//        InputStream inputStream = session.get(new URL(HttpsSession.PROTOCOL + HOST + RELATIVE_URL_VERBRUIK));
//        String body = Utils.toString(inputStream);
//        LOGGER.info("Response body size: " + body.length() + " bytes.");
//        LOGGER.debug("Body: " + body);
//        inputStream.close();
//
//        String tegoedBedrag = Utils.substringBetween(body, "<span class=\"usage\"><span class=\"amount\">", "</span>");
//        String[] strongList = Utils.substringsBetween(body, "<strong>", "</strong>");
//        String tegoed = null;
//        if (strongList != null) {
//            for (String strongItem : strongList) {
//                if (Utils.contains(strongItem, tegoedIndicator)) {
//                    LOGGER.debug("Gevonden strongItem: " + strongItem);
//                    String filterItem = Utils.deleteWhitespace(strongItem);
//                    tegoed = Utils.substringBefore(filterItem, tegoedIndicator);
//                }
//            }
//        }
//        LOGGER.info("Gevonden tegoed bedrag: " + tegoedBedrag);
//        LOGGER.info("Gevonden tegoed: " + tegoed);
//        return tegoed;
//
//    }
//
//    private boolean login(HttpsSession session, InputStream inputStream) throws IOException, URISyntaxException {
//        LOGGER.info("***************************************");
//        LOGGER.info("**        INLOGGEN                   **");
//        LOGGER.info("***************************************");
//        String body = Utils.toString(inputStream);
//        LOGGER.debug("Response body size: " + body.length() + " bytes.");
//        LOGGER.debug("Body: " + body);
//        inputStream.close();
//        FormParserUtil.Form form = FormParserUtil.parseForm(body);
//
//        if (form == null) {
//            LOGGER.info("Kan het inlogscherm niet vinden");
//            LOGGER.info(body);
//            return false;
//        }
//        LOGGER.info("Form:");
//        LOGGER.info("Form action: " + form.action);
//        LOGGER.info("Form inputs: " + form.inputList.size());
//        FormParserUtil.Input loginInput = FormParserUtil.getLoginInput(form);
//        FormParserUtil.Input passwordInput = FormParserUtil.getPasswordInput(form);
//        if (loginInput != null && passwordInput != null) {
//            loginInput.value = username;
//            passwordInput.value = password;
//            FormParserUtil.postForm(session, form);
//            return true;
//        } else {
//            LOGGER.info("Kan login en password velden NIET vinden op het login scherm.");
//            return false;
//        }
//
//    }
}
