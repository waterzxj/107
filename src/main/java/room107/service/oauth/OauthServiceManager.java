package room107.service.oauth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.datamodel.User;
import room107.service.oauth.IOauthPlatform.ReleaseStatus;
import room107.service.semaphore.SemaphoreManager;

@CommonsLog
@Component
public class OauthServiceManager {
    static private final long DEFAULT_WAIT_TIME = 1000L * 30;
    static private final long DATA_EXPIRE_TIME = 1000L * 60 * 2;
    static private final String OAUTH_SEMAPHORE_PREFIX = "oauth_";

    @Autowired
    private SemaphoreManager semaphoreManager;

    private Map<String, ReleaseStatus> statusMap = new ConcurrentHashMap<String, ReleaseStatus>();

    public ReleaseStatus waitOauthStatus(String key) {
        ReleaseStatus status = getReleaseStatus(key);
        if (status != null) {
            return status;
        } else {
            boolean isReturned = wait(key);
            if (isReturned) {
                return getReleaseStatus(key);
            } else {
                status = new ReleaseStatus();
                status.setReturned(false);
                return status;
            }
        }
    }

    private boolean wait(String key) {
        boolean success = semaphoreManager.wait(getSemaphoreKey(key),
                DEFAULT_WAIT_TIME);
        return success;
    }

    public void release(String key, User user) {
        statusMap.put(key,
                new ReleaseStatus(true, user, System.currentTimeMillis()));
        semaphoreManager.release(getSemaphoreKey(key));
    }

    public ReleaseStatus getReleaseStatus(String key) {
        return statusMap.remove(key);
    }

    private String getSemaphoreKey(String key) {
        return OAUTH_SEMAPHORE_PREFIX + key;
    }

    @PostConstruct
    public void init() {
        Thread t = new Thread("OauthServiceManager gc thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(DATA_EXPIRE_TIME);
                    } catch (InterruptedException e) {
                    }
                    log.info("OauthServiceManager gc start.");
                    int cnt = 0;
                    long start = System.currentTimeMillis();
                    List<String> keys = new ArrayList<String>(
                            statusMap.keySet());
                    for (String key : keys) {
                        ReleaseStatus status = statusMap.get(key);
                        if (status != null) {
                            if (!semaphoreManager
                                    .contains(getSemaphoreKey(key))
                                    && start - status.getCreatedTimestamp() > DATA_EXPIRE_TIME) {
                                statusMap.remove(key);
                                cnt++;
                            }
                        }
                    }
                    log.info("OauthServiceManager gc finished, collect total "
                            + cnt + " items, elapse "
                            + (System.currentTimeMillis() - start) + " ms.");
                }
            }
        };
        t.start();
    }
}
