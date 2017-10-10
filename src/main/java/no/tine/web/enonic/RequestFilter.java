package no.tine.web.enonic;

import com.enonic.xp.annotation.Order;
import com.enonic.xp.web.filter.OncePerRequestFilter;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        chain.doFilter(req, res);
        long stop = System.currentTimeMillis();

        String sessionId = getSessionId(req);
        String ip = getIP(req);
        String parameters = getRequestParameters(req);
        String host = getHost(req);
        String time = getTimeDifferenceInSec(start, stop);
        String url = req.getPathInfo() + "" + parameters;
        int statusCode = res.getStatus();
     
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

        Enumeration en = request.getParameterNames();

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
}