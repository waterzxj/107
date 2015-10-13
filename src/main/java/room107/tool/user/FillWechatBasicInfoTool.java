/**
 * 
 */
package room107.tool.user;

import java.io.IOException;
import java.util.List;

import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import room107.datamodel.User;
import room107.datamodel.WxUser;
import room107.datamodel.WxUserBasicInfo;
import room107.util.QiniuUploadUtils;
import room107.util.StringUtils;
import room107.util.WebUtils;
import room107.wechat.AccessTokenManager;

/**
 * @author yanghao
 */
@CommonsLog
public class FillWechatBasicInfoTool {

    @Setter
    private SessionFactory sessionFactory;

    @Setter
    private AccessTokenManager appTokenManager = new AccessTokenManager();

    public void run() {
        Session session = sessionFactory.openSession();
        @SuppressWarnings("unchecked")
        List<WxUser> wxUsers = session.createCriteria(WxUser.class).list();
        session.close();
        for (WxUser wxUser : wxUsers) {
            if (StringUtils.isEmpty(wxUser.getOpenId())
                    || StringUtils.isEmpty(wxUser.getUsername()))
                continue;
            session = sessionFactory.openSession();
            try {
                User user = (User) session.createCriteria(User.class)
                        .add(Restrictions.eq("username", wxUser.getUsername()))
                        .uniqueResult();
                if (user == null) {
                    log.info("discard " + wxUser.getUsername()
                            + ", no such user.");
                    continue;
                }
                String url = String
                        .format("https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid=%s&lang=zh_CN",
                                appTokenManager.getAccessToken(),
                                wxUser.getOpenId());
                WxUserBasicInfo basicInfo = WebUtils.getJson(url,
                        WxUserBasicInfo.class);
                if (basicInfo == null || basicInfo.getSubscribe() == 0) {
                    log.info("discard " + user.getUsername()
                            + ", wechat info is empty.");
                    continue;
                }
                log.info("get wx basic info : " + basicInfo);
                String nickname = StringUtils.filterUtf8Mb4(basicInfo
                        .getNickname());
                if (!StringUtils.isEmpty(nickname)) {
                    if (StringUtils.isEmpty(user.getName())) {
                        user.setName(nickname);
                    }
                }
                if (basicInfo.getSex() != 0) {
                    if (user.getGender() == null || user.getGender() == 0) {
                        user.setGender(basicInfo.getSex());
                    }
                }
                if (!StringUtils.isEmpty(basicInfo.getHeadimgurl())) {
                    String key = QiniuUploadUtils.uploadFavicon(wxUser.getUsername(),
                            basicInfo.getHeadimgurl());
                    user.setFaviconUrl(key);
                }
                Transaction tx = session.beginTransaction();
                session.update(user);
                tx.commit();
                log.info("UpdateBasicInfo: username=" + user.getUsername()
                        + ", nickname=" + user.getName() + ", gender="
                        + user.getGender() + ", favicon="
                        + user.getFaviconUrl());
            } catch (Exception e) {
                log.error("exception for openId : " + wxUser.getOpenId(), e);
            }
            session.close();
        }
    }

    public static void main(String[] args) throws IOException {
        @SuppressWarnings("resource")
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "tool-context.xml");
        FillWechatBasicInfoTool tool = (FillWechatBasicInfoTool) context
                .getBean("fillWechatBasicInfoTool");
        tool.run();
        System.exit(0);
    }

}
