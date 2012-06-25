package nl.haroid;

import android.net.http.AndroidHttpClient;
import android.util.Log;
import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ruud de Jong
 */
public final class Haring {
    private static final String LOG_TAG = "Haring";

    private String host = "www.hollandsnieuwe.nl";
    private int port = 443;
    private String schemeName = "https";
    private String relativeUrlStart = "/mijn_hollandsnieuwe";
    private String relativeUrlVerbruik = "/myaccount/subscriptionPurchaseCurrentUsageDataFeed.jsp?index=0&status=ACTIVE&profileId=";
    private String username;
    private String password;


    public String start(String username, String password) {
        this.username = username;
        this.password = password;
        AndroidHttpClient haringClient = getHaringClient();
        HttpContext localContext = getHaringContext();
        try {
            boolean ingelogd = login(haringClient, localContext);
            if (ingelogd) {
                String tegoed = haalVerbruikGegevensOp(haringClient, localContext);
                Log.i(LOG_TAG, "Gevonden tegoed: " + tegoed);
                return tegoed;
            }
        } catch (SocketTimeoutException e) {
            Log.w(LOG_TAG, "Website reageerde niet op tijd.");
            return "Website reageerde niet op tijd.";
        } catch (IOException e) {
            Log.e(LOG_TAG, "Kaput", e);
        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "Kaput", e);
        } finally {
            haringClient.getConnectionManager().shutdown();
            haringClient.close();
        }
        return "Helaas niet gevonden.";
    }

    private String haalVerbruikGegevensOp(HttpClient haringClient, HttpContext localContext) throws IOException {
        String tegoedIndicator = "minuten, smsjes of MB";
        Log.d(LOG_TAG, "**********************************");
        Log.d(LOG_TAG, "**        USAGE                 **");
        Log.d(LOG_TAG, "**********************************");
        HttpGet verbruikGet = new HttpGet(relativeUrlVerbruik);
        HttpResponse response = haringClient.execute(verbruikGet, localContext);
        HttpEntity responseEntity = response.getEntity();
        if (responseEntity == null) {
            return null;
        }
        InputStream inputStream = responseEntity.getContent();
        String body = Utils.toString(inputStream);
        String tegoedBedrag = Utils.substringBetween(body, "<span class=\"usage\"><span class=\"amount\">", "</span>");
        String[] strongList = Utils.substringsBetween(body, "<strong>", "</strong>");
        String tegoed = null;
        if (strongList != null) {
            for (String strongItem : strongList) {
                if (Utils.contains(strongItem, tegoedIndicator)) {
                    String filterItem = Utils.deleteWhitespace(strongItem);
                    tegoed = Utils.remove(filterItem, Utils.deleteWhitespace(tegoedIndicator));
                }
            }
        }
        Log.d(LOG_TAG, "Gevonden tegoed bedrag: " + tegoedBedrag);
        Log.d(LOG_TAG, "Gevonden tegoed: " + tegoed);
        return tegoed;
    }

    private boolean login(HttpClient haringClient, HttpContext localContext) throws IOException, URISyntaxException {
        Log.d(LOG_TAG, "**********************************");
        Log.d(LOG_TAG, "**        LOGIN                 **");
        Log.d(LOG_TAG, "**********************************");
        HttpGet getLogin = new HttpGet(relativeUrlStart);
        HttpResponse response = haringClient.execute(getLogin, localContext);

        HttpEntity entity = response.getEntity();
        if (entity == null) {
            Log.i(LOG_TAG, "Kan het inlogscherm niet ophalen");
            return false;
        }
        InputStream inputStream = entity.getContent();
        String body = Utils.toString(inputStream);

        Form form = parseForm(body);

        if (form == null) {
            Log.i(LOG_TAG, "Kan het inlogscherm niet vinden");
            Log.i(LOG_TAG, body);
            return false;
        }
        Log.i(LOG_TAG, "Form:");
        Log.i(LOG_TAG, "Form action: " + form.action);
        Log.i(LOG_TAG, "Form inputs: " + form.inputList.size());
        Input loginInput = getLoginInput(form);
        Input passwordInput = getPasswordInput(form);
        if (loginInput != null && passwordInput != null) {
            loginInput.value = username;
            passwordInput.value = password;
            postForm(haringClient, localContext, form);
            return true;
        } else {
            Log.i(LOG_TAG, "Kan kan login en password velden vinden op het login scherm.");
            return false;
        }
    }

    private Input getLoginInput(Form form) {
        if (form == null || form.inputList == null) {
            return null;
        }
        for (Input input : form.inputList) {
            if (input.getType() == InputType.TEXT) {
                if (Utils.containsIgnoreCase(input.getName(), "login")) {
                    Log.i(LOG_TAG, "Possible login input: " + input.getName());
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
                    Log.i(LOG_TAG, "Possible password input: " + input.getName());
                    return input;
                }
            }
        }
        return null;
    }

    private void postForm(HttpClient haringClient, HttpContext localContext, Form form) throws IOException, URISyntaxException {
        Log.i(LOG_TAG, "Raw form action: " + form.action);
        HttpRequest requestSent = (HttpRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST);
        URI previousRequestUri = new URI(requestSent.getRequestLine().getUri());
        URI resolvedFormAction = previousRequestUri.resolve(form.action);
        Log.i(LOG_TAG, "Posting form to: " + resolvedFormAction.toString());

        HttpPost post = new HttpPost(resolvedFormAction);
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        for (Input input : form.inputList) {
            formparams.add(new BasicNameValuePair(input.getName(), input.getValue()));
        }
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, "UTF-8");
        post.setEntity(entity);
        haringClient.execute(post, localContext);
    }

    private Form parseForm(String body) {
        String entireForm = Utils.substringBetween(body, "<form ", "</form>");
        String formHeader = Utils.substringBetween(body, "<form ", ">");
        if (entireForm == null || formHeader == null) {
            return null;
        }
        Form response = new Form();
        String formAction = Utils.substringBetween(formHeader, "action=\"", "\"");
        response.action = formAction;
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

    private AndroidHttpClient getHaringClient() {
        String userAgent = "Mozilla/5.0(Linux; U; Android 2.2; en-gb)";
        AndroidHttpClient haringClient = AndroidHttpClient.newInstance(userAgent);
        //((AndroidHttpClient) haringClient).setRedirectStrategy(new LaxRedirectStrategy());

        // Setup redirects
        haringClient.getParams().setParameter(ClientPNames.DEFAULT_HOST, new HttpHost(host, port, schemeName));
        haringClient.getParams().setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, Boolean.TRUE);
//        haringClient.getParams().setIntParameter("http.socket.timeout", 5);
//        haringClient.getParams().setIntParameter("http.connection.timeout=20000", 5);

        return haringClient;
    }

    private HttpContext getHaringContext() {
        // Setup cookie store
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        return localContext;
    }

    enum InputType {
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