package room107.service.semaphore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

/**
 * manager for semaphores.
 * @author YangHao
 */
@Component
public class SemaphoreManager {
    
    private static final long DEFAULT_WAIT_TIME = 1000L * 60 * 5;
    
    private Map<String, Semaphore> semaphores = new ConcurrentHashMap<String, Semaphore>();
    
    public boolean contains(String key) {
        return semaphores.containsKey(key);
    }
    
    public boolean wait(String key) {
    	return wait(key, DEFAULT_WAIT_TIME);
    }
    
    public boolean wait(String key, long waitTime){
        boolean success = false;
        try {
            Semaphore s = new Semaphore(0);
            semaphores.put(key, s);
            success = s.tryAcquire(waitTime, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            success = false;
        } finally {
            semaphores.remove(key);
        }
        return success;
    }
    
    public void release(String key) {
        Semaphore s = semaphores.get(key);
        if (s != null) s.release();
    }

}
