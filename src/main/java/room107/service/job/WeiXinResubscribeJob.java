package room107.service.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import room107.dao.IUserDao;
import room107.dao.IWxSubscribeDao;
import room107.dao.IWxUserDao;
import room107.dao.house.IHouseDao;
import room107.datamodel.AuditStatus;
import room107.datamodel.House;
import room107.datamodel.RentStatus;
import room107.datamodel.User;
import room107.datamodel.UserType;
import room107.datamodel.WxUser;
import room107.service.api.weixin.SessionStatus;
import room107.service.api.weixin.WeiXinNotifier;
import room107.service.api.weixin.WeiXinNotifier.NotifyMessage;
import room107.service.api.weixin.WeiXinService;
import room107.service.api.weixin.response.TextJsonResponse;
import room107.service.api.weixin.response.TextResponse;
import room107.service.api.weixin.response.TextResponse.Macro;
import room107.util.StringUtils;

/**
 * Notify to wx_users.
 * 
 * @author WangXiao
 */
@CommonsLog
@Component
public class WeiXinResubscribeJob extends SimpleJob {

    @Autowired
    private IWxUserDao wxUserDao;

    @Autowired
    private IUserDao userDao;

    @Autowired
    private IHouseDao houseDao;

    @Autowired
    private WeiXinService weiXinService;

    @Autowired
    private WeiXinNotifier weiXinNotifier;

    @Override
    public void run() {
        notifySubscriber();
    }

    /**
     * send message to the wx_users who have no action in 48 hours
     */
    private void notifySubscriber() {
        try {
            Date date = new Date();
            List<WxUser> wxUsers = wxUserDao
                    .getWxUsers(DateUtils.addHours(date,
                            -IWxSubscribeDao.SUBSCRIBE_NOTIFY_TIME), DateUtils
                            .addHours(date,
                                    -IWxSubscribeDao.SUBSCRIBE_NOTIFY_TIME + 1));

            List<String> usernames = new ArrayList<String>();
            for (WxUser wxUser : wxUsers) {
                if (StringUtils.isNotEmpty(wxUser.getUsername())) {
                    usernames.add(wxUser.getUsername());
                }
            }
            List<User> users = userDao.getUsers(usernames);
            Map<String, User> name2User = new HashMap<String, User>();
            for (User user : users) {
                name2User.put(user.getUsername(), user);
            }

            WeiXinService.WEIXIN_LOG.info("Notify re-subscribe: "
                    + wxUsers.size());
            for (WxUser wxUser : wxUsers) {
                String openId = wxUser.getOpenId();

                // notify to close house
                if (StringUtils.isNotEmpty(wxUser.getUsername())) {
                    User user = name2User.get(wxUser.getUsername());
                    if (user != null && UserType.isLandlord(user.getType())) {
                        List<House> houses = houseDao.getHouses(user
                                .getUsername());
                        if (CollectionUtils.isNotEmpty(houses)) {
                            House house = houses.get(0);
                            if (house.getStatus() == RentStatus.OPEN.ordinal()
                                    && house.getAuditStatus() == AuditStatus.ACCEPTED
                                            .ordinal()) {
                                wxUserDao.updateSessionStatus(openId,
                                        SessionStatus.CONFIRM_HOUSE_MANAGE
                                                .ordinal());
                                String content = new TextJsonResponse(openId,
                                        TextResponse.MESSAGE_MANAGE_HOUSE)
                                        .toString();
                                weiXinNotifier
                                        .notifySubscriber(new NotifyMessage(
                                                openId, content, true));
                                continue;
                            }
                        }
                    }
                }

                // notify to resubscribe position
                List<String> subscription = weiXinService
                        .getSubscription(openId);
                if (!subscription.isEmpty()) {
                    wxUserDao.updateSessionStatus(openId,
                            SessionStatus.CONFIRM_RESUBSCRIBE.ordinal());
                    String hint = TextResponse.MESSAGE_CONFIRM_RESUBSCRIBE
                            .replace(Macro.SUBSCRIPTION,
                                    StringUtils.join(subscription, ' '));
                    String content = new TextJsonResponse(openId, hint)
                            .toString();
                    weiXinNotifier.notifySubscriber(new NotifyMessage(openId,
                            content, true));
                }
            }
        } catch (Exception e) {
            log.error("send notifySubscriber failed", e);
        }
    }
}
