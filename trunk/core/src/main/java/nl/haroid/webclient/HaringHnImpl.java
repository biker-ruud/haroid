package nl.haroid.webclient;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public final class HaringHnImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(HaringHnImpl.class);
    private static final String SCHEME = "https://";
    private static final String HOST = "www.hollandsnieuwe.nl";
    private static final String RELATIVE_URL_START = "/mijn_hollandsnieuwe";
    private static final String RELATIVE_URL_VERBRUIK = "/myaccount/subscriptionPurchaseCurrentUsageDataFeed.jsp?index=0&status=ACTIVE&profileId=";

    private String username;
    private String password;


    public String start(String username, String password) {
        this.username = username;
        this.password = password;

        HttpsSession session = null;
        try {
            session = new HttpsSession(new URL(SCHEME + HOST));
            InputStream inputStream = session.connect(new URL(SCHEME + HOST + RELATIVE_URL_START));
            if (inputStream != null) {
                login(session, inputStream);
                String tegoed = haalVerbruikGegevensOp(session);
                LOGGER.debug("Tegoed: " + tegoed);
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
        InputStream inputStream = session.get(new URL(SCHEME + HOST + RELATIVE_URL_VERBRUIK));
        String body = Utils.toString(inputStream);
        LOGGER.info("Response body size: " + body.length() + " bytes.");
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
        Form form = parseForm(body);

        if (form == null) {
            LOGGER.info("Kan het inlogscherm niet vinden");
            LOGGER.info(body);
            return false;
        }
        LOGGER.info("Form:");
        LOGGER.info("Form action: " + form.action);
        LOGGER.info("Form inputs: " + form.inputList.size());
        Input loginInput = getLoginInput(form);
        Input passwordInput = getPasswordInput(form);
        if (loginInput != null && passwordInput != null) {
            loginInput.value = username;
            passwordInput.value = password;
            postForm(session, form);
            return true;
        } else {
            LOGGER.info("Kan login en password velden NIET vinden op het login scherm.");
            return false;
        }

    }

    private void postForm(HttpsSession session, Form form) throws IOException, URISyntaxException {
        LOGGER.info("Raw form action: " + form.action);
        URI previousRequestUri = session.getRequestUrl().toURI();
        URI resolvedFormAction = previousRequestUri.resolve(form.action);
        LOGGER.info("Posting form to: " + resolvedFormAction.toString());

        Map<String, String> postParamMap = new HashMap<String, String>();
        for (Input input : form.inputList) {
            LOGGER.debug("form param: " + input.getName() + ": " + input.getValue());
            postParamMap.put(input.getName(), input.getValue());
        }
        InputStream inputStream = session.post(resolvedFormAction.toURL(), postParamMap);
        inputStream.close();
    }

    private Input getLoginInput(Form form) {
        if (form == null || form.inputList == null) {
            return null;
        }
        for (Input input : form.inputList) {
            if (input.getType() == InputType.TEXT) {
                if (Utils.containsIgnoreCase(input.getName(), "login")) {
                    LOGGER.debug("Possible login input: " + input.getName());
                    return input;
                }
            }
        }
        return null;
    }

    private Input getPasswordInput(Form form) {
        if (form == null || form.inputList == null) {
            return null;
        }
        for (Input input : form.inputList) {
            if (input.getType() == InputType.PASSWORD) {
                if (Utils.containsIgnoreCase(input.getName(), "password")) {
                    LOGGER.debug("Possible password input: " + input.getName());
                    return input;
                }
            }
        }
        return null;
    }

    private Form parseForm(String body) {
        String entireForm = Utils.substringBetween(body, "<form ", "</form>");
        String formHeader = Utils.substringBetween(body, "<form ", ">");
        if (entireForm == null || formHeader == null) {
            return null;
        }
        Form response = new Form();
        response.action = Utils.substringBetween(formHeader, "action=\"", "\"");
        response.inputList = parseInputs(entireForm);
        return response;
    }

    private List<Input> parseInputs(String entireForm) {
        String[] inputStringArray = Utils.substringsBetween(entireForm, "<input ", ">");
        List<Input> inputs = new ArrayList<Input>();
        for (String inputString : inputStringArray) {
            String name = Utils.substringBetween(inputString, "name=\"", "\"");
            String value = Utils.substringBetween(inputString, "value=\"", "\"");
            String type = Utils.substringBetween(inputString, "type=\"", "\"");
            InputType inputType = InputType.UNKNOWN;
            if (Utils.equalsIgnoreCase(type, InputType.HIDDEN.toString())) {
                inputType = InputType.HIDDEN;
            } else if (Utils.equalsIgnoreCase(type, InputType.TEXT.toString())) {
                inputType = InputType.TEXT;
            } else if (Utils.equalsIgnoreCase(type, InputType.SUBMIT.toString())) {
                inputType = InputType.SUBMIT;
            } else if (Utils.equalsIgnoreCase(type, InputType.PASSWORD.toString())) {
                inputType = InputType.PASSWORD;
            } else if (Utils.equalsIgnoreCase(type, InputType.CHECKBOX.toString())) {
                inputType = InputType.CHECKBOX;
            }

            if (name != null && value != null) {
                inputs.add(new Input(name, value, inputType));
            }
        }
        return inputs;
    }

    private enum InputType {
        TEXT, HIDDEN, SUBMIT, PASSWORD, CHECKBOX, UNKNOWN
    }

    private class Form {
        String action;
        List<Input> inputList;
    }

    private class Input {
        String name;
        String value;
        InputType type;

        Input(String name, String value, InputType type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public String getName() {
            return this.name;
        }

        public String getValue() {
            return this.value;
        }

        public InputType getType() {
            return this.type;
        }
    }

}
