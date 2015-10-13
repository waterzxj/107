package room107.tool.user;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.IWxUserDao;
import room107.datamodel.WxUser;
import room107.service.api.weixin.SessionStatus;
import room107.tool.AutowiredTest;
import room107.util.EncryptUtils;

/**
 * @author WangXiao
 */
public class CorrectWxUserOpenId extends AutowiredTest {

    @Autowired
    private IWxUserDao wxUserDao;

    @Test
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    @Rollback(value = false)
    public void main() throws IOException {
        List<WxUser> users = wxUserDao.getAll(WxUser.class);
        int count = 0;
        for (WxUser user : users) {
            if (user.getOpenId().length() > 33) {
                try {
                    String openId = EncryptUtils.decrypt(user.getOpenId());
                    System.out.println(user.getOpenId() + "->" + openId);
                    wxUserDao.save(new WxUser(openId, user.getUsername(), user
                            .getCreatedTime(), user.getModifiedTime(), user
                            .getRefreshTime(), null, SessionStatus.NORMAL
                            .ordinal()));
                    count++;
                } catch (Exception e) {
                    System.err.println(user.getOpenId());
                }
            }
        }
        System.out.println(count);
    }

}
