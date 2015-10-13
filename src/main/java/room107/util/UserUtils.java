package room107.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.math.RandomUtils;

import room107.datamodel.User;
import room107.datamodel.VerifyStatus;
import room107.datamodel.WxUser;
import room107.web.UserController;
import room107.web.session.SessionManager;
import room107.web.session.UnsupportedTypeException;

/**
 * @author WangXiao
 */
@CommonsLog
public class UserUtils {

    public static final int AUTH_STATUS_NOT_LOGGED_IN = 0,
            AUTH_STATUS_LOGGED_IN = 1, AUTH_STATUS_VERIFIED = 2;

    private static String COOKIE_CID = "CID";
    
    public static String formatUsername(String username) {
        if (StringUtils.isEmpty(username)) return null;
        int index = username.indexOf('@');
        if (index > 0) {
            return username.substring(0, index);
        } else {
            return username;
        }
    }

    public static String getUsername(HttpServletRequest request, HttpServletResponse response) {
        User user = getUser(request, response);
        return user == null ? null : user.getUsername();
    }

    public static User getUser(HttpServletRequest request, HttpServletResponse response) {
        return SessionManager.getSessionValue(request, response, User.class.getSimpleName());
    }
    
    public static void updateUser(HttpServletRequest request, HttpServletResponse response, User user) {
        try {
            SessionManager.setSessionValue(request, response, User.class.getSimpleName(), user);
        } catch (UnsupportedTypeException e) {
            //never happened
        }
    }

    public static WxUser getWxUser(HttpServletRequest request, HttpServletResponse response) {
        return SessionManager.getSessionValue(request, response, WxUser.class.getSimpleName());
    }
    
    public static void updateWxUser(HttpServletRequest request, HttpServletResponse response, WxUser wxUser) {
        try {
            SessionManager.setSessionValue(request, response, WxUser.class.getSimpleName(), wxUser);
        } catch (UnsupportedTypeException e) {
            //never happened
        }
    }

    /**
     * For logs.
     */
    public static String getUserSignature(HttpServletRequest request, HttpServletResponse response) {
        return "username=" + getUsername(request, response) + ", cookieId="
                + getCookieId(request, response);
    }

    /**
     * For logs.
     */
    public static String getUserSignature(User user, String euid) {
        StringBuilder builder = new StringBuilder();
        if (user != null) {
            builder.append("username=").append(user.getUsername()).append(", ");
        }
        return builder.append("euid=").append(euid).toString();
    }

    public static boolean isLoggedIn(HttpServletRequest request, HttpServletResponse response) {
        return AUTH_STATUS_NOT_LOGGED_IN != getAuthenticationStatus(request, response);
    }

    public static int getAuthenticationStatus(HttpServletRequest request, HttpServletResponse response) {
        User user = getUser(request, response);
        if (user == null) {
            return AUTH_STATUS_NOT_LOGGED_IN;
        }
        Integer verifyStatus = user.getVerifyStatus();
        if (verifyStatus == null || verifyStatus.intValue() == VerifyStatus.UNVERIFIED.ordinal()) {
            return AUTH_STATUS_LOGGED_IN;
        }
        return AUTH_STATUS_VERIFIED;
    }

    /**
     * Update session to logged in status.
     * 
     * @param user
     *            user to login, loaded from DB
     * @param request
     *            assert non-null
     * @param response
     *            assert non-null
     */
    public static void login(User user, WxUser wxUser,
            HttpServletRequest request, HttpServletResponse response) {
        if (user == null) {
            log.error("Login failed: null user, request="
                    + request.getRequestURI());
            return;
        }
        /*
         * session
         */
        UserUtils.updateUser(request, response, user);
        if (wxUser != null) {
            UserUtils.updateWxUser(request, response, wxUser);
        }
        // TODO set expire for mobile
        /*
         * cookie
         */
        WebUtils.setCookie(response, UserController.COOKIE_LOGIN_USERNAME,
                user.getUsername());
        String message = "Login: username=" + user.getUsername()
                + ", cookieId=" + UserUtils.getCookieId(request, response)
                + ", IP=" + WebUtils.getRealIp(request);
        if (wxUser != null) {
            message += ", openId=" + wxUser.getOpenId();
        }
        UserBehaviorLog.BASIC.info(message);
    }

    /**
     * @param password
     *            non-null
     */
    public static String encryptPassword(String password) {
        Validate.notNull(password);
        return Long.toString(Md5Utils.sign64bit(password.getBytes()), 16);
    }
    
    /**
     * Check whether the cookie has cookie id.
     * @param request
     * @param response
     * @return boolean
     */
    public static boolean containCookieId(HttpServletRequest request,
            HttpServletResponse response){
        Validate.notNull(request);
        Validate.notNull(response);
        Cookie cookie = WebUtils.getCookie(request, COOKIE_CID);
        if(cookie==null){        	
        	return false;
        }
        return true;
    }

    public static String getCookieId(HttpServletRequest request,
            HttpServletResponse response) {
        Validate.notNull(request);
        Validate.notNull(response);
        Cookie cookie = WebUtils.getCookie(request, COOKIE_CID);
        // create if not exist
        if (cookie == null) {
            String cookieSeed = RandomUtils.nextInt() + request.getRemoteAddr()
                    + System.currentTimeMillis();
            long cookieId = Math.abs(Md5Utils.sign64bit(cookieSeed.getBytes()));
            cookie = WebUtils.setCookie(response, COOKIE_CID,
                    Long.toString(cookieId));
        }
        return cookie.getValue();
    }

    /**
     * @return null when unknown
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            if (userAgent.contains("msie")) {
                return "ie";
            } else if (userAgent.contains("firefox")) {
                return "firefox";
            } else if (userAgent.contains("chrome")) {
                return "chrome";
            }
        }
        return null;
    }

    /**
     * @return 0 when unknown
     */
    public static int getIEVersion(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            try {
                int i = userAgent.indexOf("msie");
                if (i > 0) {
                    i += 4;
                    String version = userAgent.substring(i, i + 3);
                    return (int) Float.parseFloat(version);
                }
            } catch (Exception e) {
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        // 123456:
        // -54a745b6c723f61f
        String pwd = "Hilary";
        System.out.println(encryptPassword(pwd));
    }

}
