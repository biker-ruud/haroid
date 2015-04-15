package nl.haroid.webclient;

import nl.haroid.common.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ruud de Jong
 */
public final class HttpsSession {

    public static final String PROTOCOL = "https://";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsSession.class);
    private static final int TIMEOUT = 20000;
    private static final String COOKIE_SET_HEADER = "Set-Cookie";
    private static final String COOKIE_REQUEST_HEADER = "Cookie";
    private static final String COOKIE_NAME_SEPARATOR = "=";
    private static final String COOKIE_VALUE_SEPARATOR = ";";
    private static final String REDIRECT_LOCATION_HEADER = "Location";
    private static final String ACCEPT_CHARSET = "Accept-Charset";
    private static final String UTF_8 = "utf-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String POST_CONTENT_TYPE = "application/x-www-form-urlencoded";
    private static final String POST_PARAM_VALUE_SEPARATOR = "&";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String URL_QUERY_SEPARATOR = "?";

    private SessionState state;
    private String host;
    private Map<String, String> cookieMap;
    private URL requestUrl;

    public enum HttpMethod {
        GET,
        POST
    }

    private enum SessionState {
        DISCONNECTED,
        CONNECTED
    }

    /**
     * A HTTPS-session can handle only one host.
     * All cookies will be automatically processed for this host.
     * @param host the host for this session.
     */
    public HttpsSession(URL host) {
        this.host = host.getHost();
        this.cookieMap = new HashMap<String, String>();
        this.state = SessionState.DISCONNECTED;
    }

    public InputStream connect(URL url) throws IOException {
        HttpsURLConnection connection = getConnection(stripUrlToPathAndQuery(url));
        if (connection == null) {
            return null;
        }
        InputStream inputStream = connection.getInputStream();
        if (inputStream == null) {
            return null;
        }
        this.state = SessionState.CONNECTED;
        return inputStream;
    }

    public InputStream post(URL url) throws IOException {
        return post(url, Collections.EMPTY_MAP);
    }

    public InputStream post(URL url, Map<String, String> postParamMap) throws IOException {
        if (state != SessionState.CONNECTED) {
            LOGGER.info("NOT CONNECTED!");
            return null;
        }
        HttpsURLConnection connection = getConnection(url.getPath(), HttpMethod.POST, postParamMap);
        if (connection == null) {
            return null;
        }
        InputStream inputStream = connection.getInputStream();
        if (inputStream == null) {
            return null;
        }
        return inputStream;
    }

    public InputStream get(URL url) throws IOException {
        if (state != SessionState.CONNECTED) {
            LOGGER.info("NOT CONNECTED!");
            return null;
        }
        HttpsURLConnection connection = getConnection(stripUrlToPathAndQuery(url));
        if (connection == null) {
            return null;
        }
        InputStream inputStream = connection.getInputStream();
        if (inputStream == null) {
            return null;
        }
        return inputStream;
    }

    public URL getRequestUrl() {
        return this.requestUrl;
    }

    public void disconnect() {
        try {
            URL url = new URL(PROTOCOL + host);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.disconnect();
        } catch (MalformedURLException e) {
            // Nothing
        } catch (IOException e) {
            // Nothing
        } finally {
            this.state = SessionState.DISCONNECTED;
            this.cookieMap.clear();
            this.requestUrl = null;
            this.host = null;
            LOGGER.info("DISCONNECTED!");
        }
    }

    public boolean containsCookie(String cookieName) {
        return (cookieMap.get(cookieName) != null);
    }

    private HttpsURLConnection getConnection(String path) throws IOException {
        return getConnection(path, HttpMethod.GET, null);
    }

    private HttpsURLConnection getConnection(String path, HttpMethod httpMethod, Map<String, String> postParamMap) throws IOException {
        URL url = new URL(PROTOCOL + host + path);
        this.requestUrl = url;
        LOGGER.info("Connecting to: " + url);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);
        connection.setInstanceFollowRedirects(false);
        if (this.cookieMap.size() > 0) {
            List<String> cookieList = cookieMap2List(this.cookieMap);
            String cookieRequestValue = Utils.join(cookieList.toArray(new String[cookieList.size()]), COOKIE_VALUE_SEPARATOR);
            LOGGER.info("Setting cookies: " + cookieRequestValue);
            connection.setRequestProperty(COOKIE_REQUEST_HEADER, cookieRequestValue);
        }
        connection.setRequestProperty(ACCEPT_CHARSET, UTF_8);
        if (httpMethod == HttpMethod.POST) {
            LOGGER.debug("This is a POST.");
            // This is a POST
            connection.setRequestMethod(HTTP_METHOD_POST);
            connection.setRequestProperty(CONTENT_TYPE, POST_CONTENT_TYPE);
            String postParamRequestValue = "";
            if (postParamMap != null && postParamMap.size()>0) {
                List<String> postParamList = postParamMap2List(postParamMap);
                postParamRequestValue = Utils.join(postParamList.toArray(new String[postParamList.size()]), POST_PARAM_VALUE_SEPARATOR);
                LOGGER.debug("Post Param Request Value: " + postParamRequestValue);
            }
            connection.setDoOutput(true);
            Writer writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(postParamRequestValue);
            writer.close();
        }
        connection.connect();
//        debugConnection(connection);
        this.cookieMap.putAll(getCookies(connection.getHeaderFields()));
        LOGGER.info("Aantal cookies: " + this.cookieMap.size());
        if (connection.getResponseCode() >= HttpURLConnection.HTTP_MULT_CHOICE && connection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
            // This is a 3xx redirect
            String relocatedTo = connection.getHeaderField(REDIRECT_LOCATION_HEADER);
            if (relocatedTo != null) {
                if (relocatedTo.startsWith(PROTOCOL + host)) {
                    LOGGER.info("Absolute redirection");
                    URL relocateUrl = new URL(relocatedTo);
                    this.requestUrl = relocateUrl;
                    LOGGER.info("Relocated!");
                    HttpsURLConnection relocatedConnection = getConnection(stripUrlToPathAndQuery(relocateUrl));
//                    debugConnection(relocatedConnection);
                    return relocatedConnection;
                } else {
                    LOGGER.info("Relative redirection");
                    try {
                        URI previousRequestUri = this.requestUrl.toURI();
                        URI resolvedRedirectUri = previousRequestUri.resolve(relocatedTo);
                        URL relocateUrl = resolvedRedirectUri.toURL();
                        LOGGER.info("Redirect resolved to: " + relocateUrl);
                        this.requestUrl = relocateUrl;
                        LOGGER.info("Relocated!");
                        HttpsURLConnection relocatedConnection = getConnection(stripUrlToPathAndQuery(relocateUrl));
//                        debugConnection(relocatedConnection);
                        return relocatedConnection;
                    } catch (URISyntaxException e) {
                        LOGGER.warn("Could not resolve relative redirect URL.");
                    }
                }
            }
            LOGGER.warn("RELOCATION UNCLEAR");
            return null;
        } else {
            return connection;
        }
    }

    private void debugConnection(HttpsURLConnection connection) throws IOException {
        LOGGER.info("DEBUGGING CONNECTION");
        LOGGER.info("Response code: " + connection.getResponseCode());
        LOGGER.info("Response message: " + connection.getResponseMessage());
        Map<String, List<String>> headerMap = connection.getHeaderFields();
        LOGGER.info("Header fields:");
        for (Map.Entry<String, List<String>> header : headerMap.entrySet()) {
            LOGGER.info("Header: " + header.getKey());
            for (String value : header.getValue()) {
                LOGGER.info(" - " + value);
            }
        }
        Map<String, String> cookies = getCookies(connection.getHeaderFields());
        LOGGER.info("Received Cookies:");
        for (Map.Entry<String, String> cookie : cookies.entrySet()) {
            LOGGER.info("Cookie: '" + cookie.getKey() + "' = '" + cookie.getValue() + "'.");
        }
    }

    private Map<String, String> getCookies(Map<String, List<String>> headerMap) {
        if (headerMap == null) {
            return Collections.emptyMap();
        }
        Set<String> headerKeySet = headerMap.keySet();
        if (headerKeySet == null) {
            return Collections.emptyMap();
        }
        List<String> cookieList = new ArrayList<String>();
        for (String headerKey : headerKeySet) {
            if (COOKIE_SET_HEADER.equalsIgnoreCase(headerKey)) {
                cookieList.addAll(headerMap.get(headerKey));
            }
        }
        Map<String, String> cookies = new HashMap<String, String>();
        for (String cookie : cookieList) {
            String cookieName = Utils.substringBefore(cookie, COOKIE_NAME_SEPARATOR);
            String cookieValue = Utils.substringBetween(cookie, COOKIE_NAME_SEPARATOR, COOKIE_VALUE_SEPARATOR);
            cookies.put(cookieName, cookieValue);
        }
        return cookies;
    }

    private List<String> cookieMap2List(Map<String, String> cookieMap) {
        List<String> cookieList = new ArrayList<String>();
        for (Map.Entry<String, String> cookie : cookieMap.entrySet()) {
            cookieList.add(cookie.getKey() + "=" + cookie.getValue());
        }
        return cookieList;
    }

    private List<String> postParamMap2List(Map<String, String> postParamMap) throws UnsupportedEncodingException {
        List<String> postParamList = new ArrayList<String>();
        for (Map.Entry<String, String> postParam : postParamMap.entrySet()) {
            String postParamKey = URLEncoder.encode(postParam.getKey(), UTF_8);
            String postParamValue = URLEncoder.encode(postParam.getValue(), UTF_8);
            postParamList.add(postParamKey + "=" + postParamValue);
        }
        return postParamList;
    }

    private String stripUrlToPathAndQuery(URL url) {
        String strippedUrl = url.getPath();
        if (url.getQuery() != null) {
            strippedUrl += URL_QUERY_SEPARATOR + url.getQuery();
        }
        return strippedUrl;
    }


}
