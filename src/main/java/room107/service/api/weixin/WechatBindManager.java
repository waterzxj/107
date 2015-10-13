/**
 * 
 */
package room107.service.api.weixin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.datamodel.User;
import room107.service.semaphore.SemaphoreManager;

/**
 * @author yanghao
 */
@Component
@CommonsLog
public class WechatBindManager {
    
    public static final String WECHAT_SEMAPHORE_PREFIX = "wechat_";
    
    @Autowired
    private SemaphoreManager semaphore;
    
    private Map<String, User> users = new ConcurrentHashMap<String, User>();
    
    public User wait(String key) {
        if (semaphore.wait(WECHAT_SEMAPHORE_PREFIX + key)) {
            return users.remove(key);
        }
        users.remove(key);
        return null;
    }
    
    public void release(String key, User user) {
        users.put(key, user);
        semaphore.release(WECHAT_SEMAPHORE_PREFIX + key);
    }
    
    @PostConstruct
    public void init() {
        Thread t = new Thread("WechatBindManager gc thread") {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000L * 60 * 5);
                    } catch (InterruptedException e) {}
                    log.info("WechatBindManager gc start.");
                    int cnt = 0;
                    long start = System.currentTimeMillis();
                    List<String> keys = new ArrayList<String>(users.keySet());
                    for (String key : keys) {
                        if (!semaphore.contains(WECHAT_SEMAPHORE_PREFIX + key)) {
                            users.remove(key);
                            cnt++;
                        }
                    }
                    log.info("WechatBindManager gc finished, collect total "
                            + cnt + " items, elapse "
                            + (System.currentTimeMillis() - start) + " ms.");
                }
            }
        };
        t.start();
    }
    
}
