package room107.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Log user behaviors. Sample:
 * 
 * <pre>
 * UserBehaviorLog.XXX.info(&quot;Action name: username=&quot; + username + &quot;, cookieId=&quot;
 *         + cookieId + &quot;, xxx=&quot; + xxx);
 * </pre>
 * 
 * @author WangXiao
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserBehaviorLog {

    /*
     * authentication
     */
    public static Log AUTH = getLog("auth");

    /*
     * log in/out etc.
     */
    public static Log BASIC = getLog("basic");

    /*
     * house
     */
    public static Log HOUSE = getLog("house");

    public static Log HOUSE_SEARCH = getLog("house.search");

    public static Log HOUSE_VIEW = getLog("house.view");

    public static Log HOUSE_ADD = getLog("house.add");

    public static Log HOUSE_UPDATE = getLog("house.update");

    public static Log HOUSE_DELETE = getLog("house.delete");

    public static Log APPLY_FOR_HOUSE = getLog("applyForHouse");

    /*
     * lab
     */
    public static Log LAB_WEEKLY = getLog("lab.weekly");

    public static Log LAB_BROKER = getLog("lab.broker");

    public static Log LAB_SQUARE = getLog("lab.square");

    /*
     * weixin
     */
    public static Log WEIXIN = getLog("weixin");

    /*
     * system
     */
    /**
     * Statistics.
     */
    public static Log STAT = getLog("STAT");

    private static Log getLog(String behavior) {
        return LogFactory.getLog("room107.behavior." + behavior);
    }

}
