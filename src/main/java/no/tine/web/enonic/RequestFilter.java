package no.tine.web.enonic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.enonic.xp.annotation.Order;
import com.enonic.xp.web.filter.OncePerRequestFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component(immediate = true, service = Filter.class)
@Order(-1000)
@WebFilter("/*")
public final class RequestFilter extends OncePerRequestFilter {
    private final static Logger logger = LoggerFactory.getLogger(RequestFilter.class);
    private List<Pattern> patterns = new ArrayList<Pattern>();

    public RequestFilter() {
        loadRegexFilter();
    }

    @Override
    protected void doHandle(final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws Exception {
        long start = System.currentTimeMillis();
        
        // Wrap the request to cache content for body reading
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(req);
        
        chain.doFilter(wrappedRequest, res);
        long stop = System.currentTimeMillis();

        String sessionId = getSessionId(req);
        String ip = getIP(req);
        String parameters = getRequestParameters(req);
        String host = getHost(req);
        String time = getTimeDifferenceInSec(start, stop);
        String url = req.getPathInfo() + "" + parameters;
        int statusCode = res.getStatus();
        
        // Add request body for POST requests with JSON content when DEBUG logging is enabled
        if (logger.isDebugEnabled() && "POST".equalsIgnoreCase(req.getMethod())) {
            String contentType = req.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                String requestBody = getRequestBody(wrappedRequest);
                if (requestBody != null && !requestBody.trim().isEmpty()) {
                    try {
                        // Parse and potentially mask sensitive data in JSON
                        String processedBody = processJsonBody(requestBody);
                        url += "|body=" + processedBody;
                    } catch (JSONException e) {
                        logger.warn("Failed to parse JSON body: {}", e.getMessage());
                    }
                }
            }
        }
     
        if (shouldBeDisplayed(url)) {
            logger.info("T[" + time + "] S[" + sessionId + "] I[" + ip + "] H[" + host + "] C[" + statusCode + "] M[" + req.getMethod() + "] R[" + url + "]");
        }
    }

    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        String sessionId = "";

        if (session != null) {
            sessionId = session.getId();
        }

        return sessionId;
    }

    private String getTimeDifferenceInSec(long start, long stop) {
        return Double.toString((double) (stop - start) / 1000);
    }

    private String getRequestParameters(HttpServletRequest request) {
        String parameters = "";

        Enumeration<String> en = request.getParameterNames();

        int count = 0;

        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();

            if (count > 0) {
                parameters += "&";
            }

            String[] values = request.getParameterValues(name);

            for (int i = 0; i < values.length; i++) {
                String value = values[i];

                parameters += name + "=" + value;
            }

            count++;
        }

        if (count > 0) {
            parameters = "?" + parameters;
        }

        return parameters;
    }

    public String getIP(HttpServletRequest request) {
        String userIP = request.getHeader("X-Forwarded-For");
        String remoteAddr = request.getRemoteAddr();

        String ip = "";

        if (userIP == null || userIP.trim().length() == 0) {
            ip = remoteAddr;
        } else {
            ip = userIP;
        }

        return ip;
    }

    public String getHost(HttpServletRequest request) {
        String host = request.getHeader("Host");

        if (host == null) {
            host = "";
        }

        return host;
    }


    private boolean shouldBeDisplayed(String url) {
        for (Pattern pattern : patterns) {
            Matcher m = pattern.matcher(url);

            if (m.matches()) {
                return false;
            }
        }

        return true;
    }

    private void loadRegexFilter() {
        patterns.add(Pattern.compile(".*/_/asset/.*"));
        patterns.add(Pattern.compile(".*/_/image/.*"));
        patterns.add(Pattern.compile(".*/admin/.*"));
    }

    String getRequestBody(ContentCachingRequestWrapper wrappedRequest) {
        byte[] contentAsByteArray = wrappedRequest.getContentAsByteArray();
        if (contentAsByteArray.length > 0) {
            String characterEncoding = wrappedRequest.getCharacterEncoding();
            if (characterEncoding == null) {
                characterEncoding = StandardCharsets.UTF_8.name();
            }
            try {
                return new String(contentAsByteArray, characterEncoding);
            } catch (Exception e) {
                logger.warn("Failed to read request body: {}", e.getMessage());
                return null;
            }
        }
        return null;
    }

    String processJsonBody(String requestBody) throws JSONException {
        if (requestBody.trim().startsWith("[")) {
            // Handle JSON Array
            JSONArray jsonArray = new JSONArray(requestBody);
            for (int i = 0; i < jsonArray.length(); i++) {
                Object item = jsonArray.get(i);
                if (item instanceof JSONObject) {
                    JSONObject jsonObject = (JSONObject) item;
                    maskSensitiveFields(jsonObject);
                }
            }
            return jsonArray.toString();
        } else if (requestBody.trim().startsWith("{")) {
            // Handle JSON Object
            JSONObject jsonObject = new JSONObject(requestBody);
            maskSensitiveFields(jsonObject);
            return jsonObject.toString();
        } else {
            // Not valid JSON structure
            logger.warn("Request body is not valid JSON: {}", requestBody);
            return requestBody;
        }
    }

    private void maskSensitiveFields(JSONObject jsonObject) {
        // Mask password fields for security
        if (jsonObject.has("password")) {
            jsonObject.put("password", "xxxxx");
        }
    }
}