package nl.haroid.webclient;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Package private HTML form parsing and posting util.
 *
 * @author Ruud de Jong
 */
final class FormParserUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormParserUtil.class);

    private FormParserUtil() {
        // Utility class
    }

    static Form parseForm(String body) {
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

    static List<Input> parseInputs(String entireForm) {
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

            if (name != null) {
                inputs.add(new Input(name, value, inputType));
            }
        }
        return inputs;
    }

    static void postForm(HttpsSession session, Form form) throws IOException, URISyntaxException {
        LOGGER.info("Raw form action: " + form.action);
        URI previousRequestUri = session.getRequestUrl().toURI();
        URI resolvedFormAction = previousRequestUri.resolve(form.action);
        LOGGER.info("Posting form to: " + resolvedFormAction.toString());

        Map<String, String> postParamMap = new HashMap<String, String>();
        for (Input input : form.inputList) {
            LOGGER.info("form param: " + input.getName() + ": " + input.getValue());
            postParamMap.put(input.getName(), input.getValue());
        }
        InputStream inputStream = session.post(resolvedFormAction.toURL(), postParamMap);
        inputStream.close();
    }

    static Input getLoginInput(Form form) {
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

    static Input getPasswordInput(Form form) {
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

    enum InputType {
        TEXT, HIDDEN, SUBMIT, PASSWORD, CHECKBOX, UNKNOWN
    }

    static class Input {
        String name;
        String value;
        InputType type;

        Input(String name, String value, InputType type) {
            this.name = name;
            this.type = type;
            if (value == null) {
                this.value = "";
            } else {
                this.value = value;
            }
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

    static class Form {
        String action;
        List<Input> inputList;
    }


}
