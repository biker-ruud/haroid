package nl.haroid;

import android.util.Log;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ruud de Jong
 */
public final class HaringHnImpl {
//    private static final String LOG_TAG = "HaringHnImpl";
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
                System.out.println("Tegoed: " + tegoed);
            }
        } catch (MalformedURLException e) {
//            Log.e(LOG_TAG, "Kaput", e);
            e.printStackTrace();
        } catch (ClassCastException e) {
//            Log.e(LOG_TAG, "Kaput", e);
            e.printStackTrace();
        } catch (SocketTimeoutException e) {
//            Log.e(LOG_TAG, "Kaput", e);
            e.printStackTrace();
        } catch (IOException e) {
//            Log.e(LOG_TAG, "Kaput", e);
            e.printStackTrace();
        } catch (URISyntaxException e) {
//            Log.e(LOG_TAG, "Kaput", e);
            e.printStackTrace();
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
//        System.out.println("Response to fetch tegoed: ");
//        System.out.println(body);
        inputStream.close();

        String tegoedBedrag = Utils.substringBetween(body, "<span class=\"usage\"><span class=\"amount\">", "</span>");
        String[] strongList = Utils.substringsBetween(body, "<strong>", "</strong>");
        String tegoed = null;
        if (strongList != null) {
            for (String strongItem : strongList) {
                if (Utils.contains(strongItem, tegoedIndicator)) {
//                    Log.d(LOG_TAG, "Gevonden strongItem: " + strongItem);
                    String filterItem = Utils.deleteWhitespace(strongItem);
                    tegoed = Utils.substringBefore(filterItem, tegoedIndicator);
                }
            }
        }
//        Log.d(LOG_TAG, "Gevonden tegoed bedrag: " + tegoedBedrag);
//        Log.d(LOG_TAG, "Gevonden tegoed: " + tegoed);
        System.out.println("Gevonden tegoed bedrag: " + tegoedBedrag);
        System.out.println("Gevonden tegoed: " + tegoed);
        return tegoed;

    }

    private boolean login(HttpsSession session, InputStream inputStream) throws IOException, URISyntaxException {
        String body = Utils.toString(inputStream);
        inputStream.close();
        Form form = parseForm(body);

        if (form == null) {
//            Log.i(LOG_TAG, "Kan het inlogscherm niet vinden");
//            Log.i(LOG_TAG, body);
            return false;
        }
//        Log.i(LOG_TAG, "Form:");
//        Log.i(LOG_TAG, "Form action: " + form.action);
//        Log.i(LOG_TAG, "Form inputs: " + form.inputList.size());
        Input loginInput = getLoginInput(form);
        Input passwordInput = getPasswordInput(form);
        if (loginInput != null && passwordInput != null) {
            loginInput.value = username;
            passwordInput.value = password;
            postForm(session, form);
            return true;
        } else {
//            Log.i(LOG_TAG, "Kan kan login en password velden vinden op het login scherm.");
            return false;
        }

    }

    private void postForm(HttpsSession session, Form form) throws IOException, URISyntaxException {
//        Log.i(LOG_TAG, "Raw form action: " + form.action);
        System.out.println("Raw form action: " + form.action);
//        HttpRequest requestSent = (HttpRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST);
        URI previousRequestUri = session.getRequestUrl().toURI();
        URI resolvedFormAction = previousRequestUri.resolve(form.action);
//        Log.i(LOG_TAG, "Posting form to: " + resolvedFormAction.toString());

//        HttpPost post = new HttpPost(resolvedFormAction);
//        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        Map<String, String> postParamMap = new HashMap<String, String>();
        for (Input input : form.inputList) {
//            formparams.add(new BasicNameValuePair(input.getName(), input.getValue()));
            System.out.println("form param: " + input.getName() + ": " + input.getValue());
            postParamMap.put(input.getName(), input.getValue());
        }
        System.out.println("POSTing form to: " + resolvedFormAction.toString());
        InputStream inputStream = session.post(resolvedFormAction.toURL(), postParamMap);
//        System.out.println("Response to login: ");
//        System.out.println(Utils.toString(inputStream));
        inputStream.close();
//        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
//        post.setEntity(entity);
//        haringClient.execute(post, localContext);
    }

    private Input getLoginInput(Form form) {
        if (form == null || form.inputList == null) {
            return null;
        }
        for (Input input : form.inputList) {
            if (input.getType() == InputType.TEXT) {
                if (Utils.containsIgnoreCase(input.getName(), "login")) {
//                    Log.i(LOG_TAG, "Possible login input: " + input.getName());
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
//                    Log.i(LOG_TAG, "Possible password input: " + input.getName());
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
