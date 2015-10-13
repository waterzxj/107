package room107.service.api.weixin;

import java.util.Calendar;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.service.api.weixin.admin.WeiXinAdminService;
import room107.service.api.weixin.response.WeiXinResponse;
import room107.wechat.AccessTokenManager;

/**
 * Notify subscriber within transaction in an async way.
 * 
 * @author WangXiao
 */
@CommonsLog
@Component
public class WeiXinNotifier {

    @Autowired
    private WeiXinAdminService weiXinAdminService;

    @Autowired
    private AccessTokenManager accessTokenManager;

    private BlockingQueue<NotifyMessage> queue = new LinkedBlockingQueue<NotifyMessage>();

    public WeiXinNotifier() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        /*
                         * sleep in bad time
                         */
                        int hour = Calendar.getInstance().get(
                                Calendar.HOUR_OF_DAY);
                        if (hour >= 22 || hour < 8) {
                            // 5 minutes
                            Thread.sleep(5 * 60 * 1000);
                            continue;
                        }
                        /*
                         * work
                         */
                        NotifyMessage message = queue.take();
                        handleNotify(message);
                        Thread.sleep(1000); // 1 second
                    } catch (Exception e) {
                        log.error("Take NotifyMessage failed", e);
                    }
                }
            }
        }, "NotifyMessageConsumer").start();
    }

    public void notifySubscriber(NotifyMessage message) {
        // if (!weiXinAdminService.isAdmin(openId)) {
        // WeiXinService.WEIXIN_LOG.info(message);
        // return;
        // }
        Validate.notNull(message);
        if (message.isLazy()) {
            if (!queue.offer(message)) {
                log.error("Enqueue failed: " + message);
            }
        } else {
            handleNotify(message);
        }
    }

    /**
     * Notify within transaction.
     */
    private void handleNotify(NotifyMessage message) {
        Validate.notNull(message);
        WeiXinService.WEIXIN_LOG.info("Handle notify: " + message);
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token="
                    + accessTokenManager.getAccessToken();
            WeiXinResponse response = WeiXinUtils.post(url,
                    message.getContent());
            if (response != null) {
                log.debug(response);
                if (response.errcode == WeiXinResponse.CODE_OK) {
                    return;
                }
                if (response.errcode == WeiXinResponse.CODE_INVALID_CREDENTIAL) {
                    accessTokenManager.updateAccessToken();
                }
            } else {
                log.warn("Null weixin response: message=" + message);
            }
        } catch (Exception e) {
            log.error("Notify subscriber failed: message=" + message, e);
        }
        /*
         * re-notify
         */
        message.setLazy(true);
        notifySubscriber(message);
    }

    @AllArgsConstructor
    @Data
    public static class NotifyMessage {
        private String openId, content;
        private boolean lazy = true;
    }

}
