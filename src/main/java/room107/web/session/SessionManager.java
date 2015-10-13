/**
 * 
 */
package room107.web.session;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.util.StringUtils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import room107.util.SerializeUtils;
import room107.util.WebUtils;

/**
 * @author yanghao
 *
 */
@CommonsLog
public class SessionManager {
    
    private static final String SESSION_COOKIE = "107ROOM_SESSION_ID";
    
    private static final String REDIS_HOST = "redis-server";
    
    private static final String SESSION_PREFIX = "session_";
    
    private static final int SESSION_EXPIRE_TIME_IN_SECONDS = 60 * 60;
    
    private static final String COOKIE_CODES = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    private static final int SESSION_COOKIE_LENGTH = 25;
    
    private static final JedisPool pool;
    
    static {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setBlockWhenExhausted(false);
        config.setTestOnReturn(true);
        pool = new JedisPool(config, REDIS_HOST);
    }
    
    private static String generateSessionCookie() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SESSION_COOKIE_LENGTH; i++) {
            sb.append(COOKIE_CODES.charAt(rand.nextInt(COOKIE_CODES.length())));
        }
        return sb.toString();
    }
    
    private static boolean remove(String id) {
        if (StringUtils.isEmpty(id)) return false;
        Jedis jedis = null;
        id = SESSION_PREFIX + id;
        try {
            jedis = pool.getResource();
            jedis.del(id.getBytes());
        } catch(Exception e) {
            log.error("remove session failed. id = " + id, e);
            return false;
        } finally {
            if (jedis != null) {
                pool.returnResourceObject(jedis);
            }
        }
        return true;
    }
    
    private static boolean set(String id, Session value) {
        if (StringUtils.isEmpty(id) || value == null) return false;
        Jedis jedis = null;
        id = SESSION_PREFIX + id;
        try {
            jedis = pool.getResource();
            byte[] v = SerializeUtils.serialize(value);
            jedis.set(id.getBytes(), v);
            jedis.expire(id, SESSION_EXPIRE_TIME_IN_SECONDS);
        } catch(Exception e) {
            log.error("set session failed. id = " + id, e);
            return false;
        } finally {
            if (jedis != null) {
                pool.returnResourceObject(jedis);
            }
        }
        return true;
    }
    
    private static Session get(String id) {
        if (StringUtils.isEmpty(id)) return null;
        Jedis jedis = null;
        id = SESSION_PREFIX + id;
        try {
            jedis = pool.getResource();
            byte[] bytes = jedis.get(id.getBytes());
            Session session = SerializeUtils.deseialize(bytes, Session.class);
            if (session != null) {
                jedis.expire(id, SESSION_EXPIRE_TIME_IN_SECONDS);
            } else {
                session = new Session();
            }
            return session;
        } catch(Exception e) {
            log.error("get session failed. key = " + id, e);
        } finally {
            if (jedis != null) {
                pool.returnResourceObject(jedis);
            }
        }
        return null;
    }
    
    private static String getCookie(HttpServletRequest request, HttpServletResponse response) {
        String id = WebUtils.getCookieValue(request, SESSION_COOKIE);
        if (StringUtils.isEmpty(id)) {
            id = (String)request.getAttribute(SESSION_COOKIE);
            if (StringUtils.isEmpty(id)) {
                id = generateSessionCookie();
                request.setAttribute(SESSION_COOKIE, id);
                WebUtils.setCookie(response, SESSION_COOKIE, id);
            }
        }
        return id;
    }

    public static Session getSession(HttpServletRequest request, HttpServletResponse response) {
        String id = getCookie(request, response);
        return get(id);
    }

    public static <T> T getSessionValue(HttpServletRequest request, HttpServletResponse response, String key) {
        Session session = getSession(request, response);
        if (session == null) return null;
        return session.get(key);
    }

    public static <T> T getSessionValue(HttpServletRequest request, HttpServletResponse response, String key,
            T defaultValue) {
        T value = getSessionValue(request, response, key);
        return value == null ? defaultValue : value;
    }

    public static void setSessionValue(HttpServletRequest request, HttpServletResponse response, String key,
            Object value) throws UnsupportedTypeException {
        String id = getCookie(request, response);
        Session session = get(id);
        if (session != null) {
            session.set(key, value);
            set(id, session);
        }
    }

    public static void removeSession(HttpServletRequest request,
            HttpServletResponse response) {
        String id = getCookie(request, response);
        remove(id);
    }
    
    public static void removeSessionValue(HttpServletRequest request, HttpServletResponse response, String key) {
        String id = getCookie(request, response);
        Session session = get(id);
        if (session != null) {
            session.remove(key);
            set(id, session);
        }
    }

}
