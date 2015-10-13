/**
 * 
 */
package room107.util;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import room107.datamodel.Location;
import room107.datamodel.LogType;
import room107.datamodel.Platform;
import room107.datamodel.WxUser;

/**
 * @author yanghao
 */
public class UserStatLog {
    
    private static final Log error = LogFactory.getLog(UserStatLog.class);

    private static final Log logger = LogFactory.getLog("event");

    public static void log(LogType type, Date timestamp, String username,
            String openId, String ip, Platform platform, Long houseId,
            Long roomId, Double longitude, Double latitude, String moreinfo) {
        try {
            String csv = CSVUtils.toCSV(type, timestamp, username, openId, ip, platform,
                    houseId, roomId, longitude, latitude, moreinfo);
            logger.info(csv);
        } catch(Exception e) {
            error.error(e, e);
        }
    }
    
    public static void log(LogType type, String username, String openId) {
        log(type, username, openId, null);
    }
    
    public static void log(LogType type, String username, String openId, String moreinfo) {
        log(type, new Date(), username, openId, null, Platform.wechat, null, null, null, null, moreinfo);
    }
    
    public static void log(HttpServletRequest request, HttpServletResponse response,
            LogType type, Long houseId, Long roomId, Double latitude, Double longitude, String moreinfo) {
        try {
            String username = UserUtils.getUsername(request, response);
            WxUser wxUser = UserUtils.getWxUser(request, response);
            String openId = null;
            if (wxUser != null) {
                openId = wxUser.getOpenId();
            }
            String ip = WebUtils.getRealIp(request);
            Platform platform = WebUtils.getPlatform(request);
            
            log(type, new Date(), username, openId, ip, platform, houseId, roomId, latitude, longitude, moreinfo);
        } catch(Exception e) {
            error.error(e, e);
        }
    }
    
    public static void log(HttpServletRequest request, HttpServletResponse response, LogType type) {
        log(request, response, type, null, null, null, null, null);
    }
    
    public static void log(HttpServletRequest request, HttpServletResponse response,
            LogType type, String moreinfo) {
        log(request, response, type, null, null, null, null, moreinfo);
    }
    
    public static void log(HttpServletRequest request, HttpServletResponse response,
            LogType type, Location location, String moreinfo) {
        try {
            Double lat = null, lng = null;
            if (location != null) {
                lng = location.getX();
                lat = location.getY();
            }
            log(request, response, type, null, null, lng, lat, moreinfo);
        } catch(Exception e) {
            error.error(e, e);
        }
    }
    
    public static void log(HttpServletRequest request, HttpServletResponse response,
            LogType type, Long houseId, Long roomId) {
        Double latitude = WebUtils.getDouble(request, "lat", null);
        Double longitude = WebUtils.getDouble(request, "lng", null);
        log(request, response, type, houseId, roomId, latitude, longitude, null);
    }
    
    public static void log(HttpServletRequest request, HttpServletResponse response,
            LogType type, Long houseId, Long roomId, String moreinfo) {
        log(request, response, type, houseId, roomId, null, null, moreinfo);
    }
    
}
