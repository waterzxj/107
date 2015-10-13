package room107.service.api.weixin;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.service.api.weixin.request.ScanEventRequest.Scene;
import room107.util.JsonUtils;
import room107.util.StringUtils;
import room107.util.WebUtils;
import room107.wechat.AccessTokenManager;

/**
 * @author HaoYang
 */
@Component
@CommonsLog
public class Code2TicketManager {

    @Autowired
    private AccessTokenManager appTokenManager;

    /**
     * userId -> ticket.
     */
    private Map<Long, Ticket> cache;

    public Code2TicketManager() {
        setCacheSize(10240);
    }

    @SuppressWarnings("unchecked")
    public void setCacheSize(int size) {
        cache = Collections.synchronizedMap(new LRUMap(size));
    }

    /**
     * @return null when missed in cache
     */
    public String getTicketNoneBlocked(long userId) {
        Ticket ticket = cache.get(userId);
        if (ticket == null || ticket.isExpired()) {
            return null;
        }
        return ticket.ticket;
    }

    /**
     * @return non-null normally
     */
    public String getTicketBlocked(long userId) {
        Ticket ticket = cache.get(userId);
        if (ticket == null) { // missed in cache
            ticket = getBlocked(userId);
            if (ticket != null) {
                cache.put(userId, ticket);
            }
        } else if (ticket.isExpired()) { // expired
            cache.remove(userId);
            return getTicketBlocked(userId);
        }
        if (ticket == null) return null;
        return ticket.ticket;
    }

    /**
     * TODO save all users' code files in our server
     */
    private Ticket getBlocked(long userId) {
        String response = "";
        try {
            // init
            String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="
                    + appTokenManager.getAccessToken();
            String request = String
                    .format("{\"expire_seconds\": %d, \"action_name\": \"QR_SCENE\", \"action_info\": {\"scene\": {\"scene_id\": %d}}}",
                            Ticket.EXPIRE_SEC, Scene.sceneValue(userId));
            // post
            String json = WebUtils.postJson(url, request);
            response = json;
            if (StringUtils.isEmpty(json) || json.contains("40001")) {
                // update token
                appTokenManager.updateAccessToken();
                return null;
            }
            // encode
            Ticket result = JsonUtils.fromJson(json, Ticket.class);
            if (result != null) {
                result.setTicket(URLEncoder.encode(result.getTicket(), "UTF-8"));
                return result;
            } else {
                appTokenManager.updateAccessToken();
                return null;
            }
        } catch (Exception e) {
            log.error("Get ticket failed: userId=" + userId + ", response = " + response, e);
            return null;
        }
    }

    /**
     * Ticket to get code.
     * 
     * @author WangXiao
     */
    @NoArgsConstructor
    @RequiredArgsConstructor
    @ToString
    private static class Ticket {

        @Getter
        @Setter
        @NonNull
        private String ticket;

        @Getter
        @Setter
        private int expire_seconds;

        private Date time = new Date();

        /**
         * As in WeiXin API.
         */
        private static int EXPIRE_SEC = 1800;

        public boolean isExpired() {
            return DateUtils.addSeconds(time, getSafeExpireSeconds())
                    .compareTo(new Date()) <= 0;
        }

        private int getSafeExpireSeconds() {
            return (int) (expire_seconds * 0.8);
        }

    }

}
