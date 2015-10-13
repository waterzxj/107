package room107.util.web;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author WangXiao
 */
@CommonsLog
public class DebugUtils {

    /**
     * With "true" value when in debug model.
     */
    public static final String DEBUG_PARAM = "debug";

    /**
     * With debug details value when activated.
     */
    public static final String DEBUG_DETAIL_PARAM = "debugDetail";

    private static final String[] INNER_HOST = { "inner-107room",
            "test-107room" };

    private static final ThreadLocal<AtomicBoolean> DEBUG_ENABLED = new ThreadLocal<AtomicBoolean>() {

        @Override
        protected AtomicBoolean initialValue() {
            return new AtomicBoolean();
        }
    };

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * @return whether debug enabled for the specified request
     */
    public static boolean isDebugEnabled(HttpServletRequest request) {
        if (request.getParameter(DEBUG_PARAM) == null)
            return false;
        String host = request.getServerName();
        for (String inner : INNER_HOST) {
            if (inner.equals(host)) {
                return true;
            }
        }
        log.warn("Unknown host try to use debug, host = " + host + ", ip = "
                + getIpAddr(request));
        return false;
    }

    /**
     * "debug=detail" to show details on page.
     */
    public static boolean isShowDebugDetail(HttpServletRequest request) {
        return "1".equals(request.getParameter(DebugUtils.DEBUG_PARAM));
    }

    /**
     * @return whether debug enabled currently
     */
    public static boolean isDebugEnabled() {
        return DEBUG_ENABLED.get().get();
    }

    /**
     * Update the debug status.
     */
    public static void setDebugEnabled(boolean debugEnabled) {
        DEBUG_ENABLED.get().set(debugEnabled);
    }

    /**
     * @param url
     *            usually parameter of #spiringUrl()
     * @return URL with wrap of "debug=0" when needed
     */
    public static String wrapDebug(String url) {
        if (url == null || !isDebugEnabled()) {
            return url;
        }
        if (url.startsWith("/static")) {
            return url;
        }
        if (url.contains("?")) {
            url += "&debug=0";
        } else {
            if (!url.endsWith("/")) {
                url += "/";
            }
            url += "?debug=0";
        }
        return url;
    }

}
