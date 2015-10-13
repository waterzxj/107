package room107.web.antispam;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.dao.ISniffUrlDao;
import room107.datamodel.SniffUrl;
import room107.service.totoro.ISniffHistoryService;
import room107.service.totoro.ISniffHistoryService.SingleSniffResult;
import room107.util.JsonUtils;
import room107.util.UserUtils;
import room107.web.session.SessionManager;
import room107.web.session.UnsupportedTypeException;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * @author WangXiao
 */
@CommonsLog
@RequestMapping("/antispam/history")
@Controller
public class SniffHistoryController {

    @Autowired
    private ISniffHistoryService sniffHistoryService;

    /**
     * Debug parameter to specify URL(s) to sniff. e.g.
     * "/antispam/history/?_url=a.com&_url=b.com"
     */
    private static final String DEBUG_URL = "url";

    /**
     * Clear the sniff-disabled status in session.
     */
    private static final String DEBUG_FORCE_SNIFF = "forceSniff";

    /**
     * Disable amend URL.
     */
    private static final String DEBUG_NO_AMEND = "noAmend";

    private static final String SESSION_KEY_DISABLE_SNIFF = "disableSniff";

    private static final String SESSION_KEY_CONFIDENT_LEVEL = "confidentLevel";

    private static final int[] CONFIDENT_LEVELS = { 10, 30, 100 };
    
    @RequestMapping(value = "/")
    public String getScript(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        /*
         * formal parameters
         */
        map.put("confidentBaseLevel", CONFIDENT_LEVELS[0]);
        Integer level = SessionManager.getSessionValue(request, response, SESSION_KEY_CONFIDENT_LEVEL, 0);
        try {
            SessionManager.setSessionValue(request, response, SESSION_KEY_CONFIDENT_LEVEL, level);
        } catch (UnsupportedTypeException e) {
            log.error(e, e);
        }
        /*
         * debug parameters
         */
        // specify sniff URL
        String[] urls = request.getParameterValues(DEBUG_URL);
        if (urls != null) {
            String url = "";
            for (String u : urls) {
                url += u + '|';
            }
            map.put(DEBUG_URL, url);
        }
        // force sniff
        if (request.getParameter(DEBUG_FORCE_SNIFF) != null) {
            SessionManager.removeSessionValue(request, response, SESSION_KEY_DISABLE_SNIFF);
        }
        map.put(DEBUG_NO_AMEND, request.getParameter(DEBUG_NO_AMEND) != null);
        return "antispam/sniff-history";
    }

    @RequestMapping(value = "/url")
    @ResponseBody
    public String getSniffUrlsJson(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map) {
        if (SessionManager.getSessionValue(request, response, SESSION_KEY_DISABLE_SNIFF) != null) {
            return null; // sniff disabled
        }
        List<SniffUrl> urls = null;
        /*
         * Get sample URLs
         */
        String debugUrl = request.getParameter(DEBUG_URL);
        if (debugUrl != null) { // debug URL
            String[] ss = StringUtils.split(debugUrl, '|');
            urls = new ArrayList<SniffUrl>();
            for (int i = 0; i < ss.length; i++) {
                SniffUrl u = new SniffUrl();
                u.setUrlId((long) -i);
                u.setUrl(ss[i]);
                urls.add(u);
            }
        } else {
            for (int i = SessionManager.getSessionValue(request, response,
                    SESSION_KEY_CONFIDENT_LEVEL, 0); i < CONFIDENT_LEVELS.length; i++) {
                urls = sniffHistoryService
                        .getSniffUrls(UserUtils.getCookieId(request, response),
                                UserUtils.getUsername(request, response), 50,
                                CONFIDENT_LEVELS[i]);
                if (urls == ISniffUrlDao.ALL_CONFIDENT) { // next level
                    try {
                        SessionManager.setSessionValue(request, response, SESSION_KEY_CONFIDENT_LEVEL, i + 1);
                    } catch (UnsupportedTypeException e) {
                        //never happened
                    }
                } else { // valid in this level
                    break;
                }
            }
            if (urls == ISniffUrlDao.ALL_CONFIDENT) {
                try {
                    SessionManager.setSessionValue(request, response, SESSION_KEY_DISABLE_SNIFF, true);
                } catch (UnsupportedTypeException e) {
                    //never happened
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Sample sniff URLs: " + urls);
        }
        /*
         * Amend sample URLs. e.g. add "a.com->www.a.com" and "www.b.com->b.com"
         */
        List<SniffUrl> amendUrls = new LinkedList<SniffUrl>(urls);
        String noAmend = request.getParameter(DEBUG_NO_AMEND);
        if (noAmend == null || noAmend.equals(Boolean.FALSE.toString())) {
            for (SniffUrl url : urls) {
                // init amend URL
                SniffUrl amendUrl = new SniffUrl(url.getUrl(),
                        SingleSniffResult.TYPE_AMEND_URL, url.getWeight(),
                        url.getModifiedTime());
                amendUrl.setUrlId(url.getUrlId());
                String s = url.getUrl();
                int i = s.indexOf("://");
                String protocol = null;
                if (i >= 0) {
                    i += 3;
                    protocol = s.substring(0, i);
                    s = s.substring(i);
                }
                if (s.startsWith("www")) { // remove "www"
                    s = s.substring(4);
                } else { // add "www"
                    s = "www." + s;
                }
                if (protocol != null) {
                    s = protocol + s;
                }
                amendUrl.setUrl(s);
                amendUrls.add(amendUrl);
            }
            if (log.isDebugEnabled()) {
                log.debug("Amended sniff URLs: " + amendUrls);
            }
        }
        return JsonUtils.toJson(amendUrls,
                SniffUrlJsonStrategy.STRATEGY);
    }

    @RequestMapping(value = "/result", method = RequestMethod.POST)
    @ResponseBody
    public String putSniffResultJson(HttpServletRequest request,
            HttpServletResponse response) {
        String resultJson = request.getParameter("result");
        log.debug("Result JSON: " + resultJson);
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        JsonArray array = parser.parse(resultJson).getAsJsonArray();
        if (array.size() > 0) {
            for (JsonElement e : array) {
                SingleSniffResult result = gson.fromJson(e,
                        SingleSniffResult.class);
                // amend URL only take effects when visited
                boolean amend = result.isAmend();
                if (!amend || (result.isAmend() && result.isVisited())) {
                    sniffHistoryService.saveHistory(
                            UserUtils.getCookieId(request, response),
                            UserUtils.getUsername(request, response), result);
                }
            }
        }
        return null;
    }

    /**
     * JSON strategy for {@link SniffUrl}.
     * 
     * @author WangXiao
     */
    private static class SniffUrlJsonStrategy implements ExclusionStrategy {

        private static final SniffUrlJsonStrategy STRATEGY = new SniffUrlJsonStrategy();

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            String name = f.getName();
            if ("urlId".equals(name) || "url".equals(name)
                    || "type".equals(name)) {
                return false;
            }
            return true;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

    }

}
