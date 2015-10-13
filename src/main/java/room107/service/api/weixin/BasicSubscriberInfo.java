/**
 * 
 */
package room107.service.api.weixin;

/**
 * @author yanghao
 */
public class BasicSubscriberInfo {
    
    public String username;
    
    public String openId;
    
    public String faviconUrl;
    
    public double distance;
    
    public double x;
    
    public double y;
    
    public String nickname;
    
    public BasicSubscriberInfo() {}
    
    public BasicSubscriberInfo(BasicSubscriberInfo other) {
        this.username = other.username;
        this.openId = other.openId;
        this.faviconUrl = other.faviconUrl;
        this.distance = other.distance;
        this.x = other.x;
        this.y = other.y;
        this.nickname = other.nickname;
    }
    
    public BasicSubscriberInfo(String username, String openId) {
        this.username = username;
        this.openId = openId;
    }
    
}
