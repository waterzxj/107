package room107.service.oauth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import room107.dao.UsernameExistException;
import room107.datamodel.User;
import room107.service.oauth.IOauthPlatform.Auth;
import room107.service.oauth.IOauthPlatform.AuthUserInfo;
import room107.service.oauth.IOauthPlatform.ReleaseStatus;
import room107.service.user.IUserService;

@Service
@Transactional
public class OauthServiceImpl implements IOauthService {
    @Autowired
    private OauthServiceManager oauthServiceManager;

    @Autowired
    private IUserService userService;

    @Autowired
    private List<IOauthPlatform> oauthPlatforms;

    IOauthPlatform oauthPlatform = null;

    Map<String, String> loginUrls = new HashMap<String, String>();

    @PostConstruct
    public void init() {
        for (IOauthPlatform oauthPlatform : oauthPlatforms) {
            loginUrls.put(oauthPlatform.getPlatform(),
                    oauthPlatform.getLoginUrl());
        }
    }

    @Override
    public Map<String, String> getLoginUrls() {
        return loginUrls;
    }

    @Override
    public ReleaseStatus waitOauthStatus(String key) {
        return oauthServiceManager.waitOauthStatus(key);
    }

    @Override
    public void confirmOauthStatus(String key, User user) {
        oauthServiceManager.release(key, user);
    }

    @Override
    public User getOauthUser(String platform, String code) {
        AuthUserInfo authUserInfo = null;
        Auth auth = null;
        User user = null;

        IOauthPlatform oauthPlatform = getOauthPlatform(platform);
        // get access_token with token
        auth = oauthPlatform.getAuth(code);
        if (auth == null) {
            return null;
        }
        user = userService.getUser(auth.getUsername());
        // register firstly if the user doesn't exists:
        if (user == null) {
            authUserInfo = oauthPlatform.getUserInfo(auth);
            try {
                user = userService.createUser(authUserInfo.getUser());
            } catch (UsernameExistException e) {
                return null;
            }
        }
        return user;
    }

    private IOauthPlatform getOauthPlatform(String platform) {
        for (int i = 0; i < oauthPlatforms.size(); i++) {
            if (oauthPlatforms.get(i).getPlatform().equals(platform)) {
                return oauthPlatforms.get(i);
            }
        }
        return null;
    }
}
