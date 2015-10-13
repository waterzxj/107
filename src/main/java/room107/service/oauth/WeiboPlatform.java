package room107.service.oauth;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import room107.datamodel.UserStatus;
import room107.util.QiniuUploadUtils;
import room107.util.WebUtils;

@CommonsLog
@Service
public class WeiboPlatform extends OauthPlatformImpl {
    private Gson gson = new Gson();
    private static final String PLATFORM = "weibo";
    private static final String AUTH_URL = "https://api.weibo.com/oauth2/authorize";
    private static final String TOKEN_URL = "https://api.weibo.com/oauth2/access_token";
    private static final String INFO_URL = "https://api.weibo.com/2/users/show.json";
    private static final String CLIENT_ID = "1692960071";
    private static final String CLIENT_SECRET = "84cd55bd6e83c5bf1f347d2c98dbd543";
    private static final String REDIRECT_URI = "http://107room.com/user/oauth/weibo/response";
    private static final String GRANT_TYPE = "authorization_code";
    private static final String RESPONSE_TYPE = "code";
    public static final String LOGIN_URL = AUTH_URL + "?client_id=" + CLIENT_ID
            + "&redirect_uri=" + REDIRECT_URI + "&response_type="
            + RESPONSE_TYPE;

    @Override
    public String getPlatform() {
        return PLATFORM;
    }

    @Override
    public String getLoginUrl() {
        return LOGIN_URL;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Auth getAuth(String code) {
        String body = "";
        Map<String, String> options = new HashMap<String, String>();
        Map<String, String> authInfo = null;
        options.put("client_id", CLIENT_ID);
        options.put("client_secret", CLIENT_SECRET);
        options.put("redirect_uri", REDIRECT_URI);
        options.put("grant_type", GRANT_TYPE);
        options.put("code", code);

        body = WebUtils.post(TOKEN_URL, options);
        authInfo = gson.fromJson(body, Map.class);

        if (!authInfo.containsKey("access_token")) {
            // access token doesn't exist
            return null;
        }
        Auth auth = new Auth();
        auth.setAccessToken(authInfo.get("access_token"));
        auth.setUid(authInfo.get("uid"));
        auth.setUsernameWithUid(authInfo.get("uid"), PLATFORM);
        return auth;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AuthUserInfo getUserInfo(Auth auth) {
        String body = null;
        Map<String, String> params = new HashMap<String, String>();
        Map<String, ? super String> responseJson = null;

        params.put("access_token", auth.getAccessToken());
        params.put("uid", auth.getUid());
        body = WebUtils.getWithHeaders(INFO_URL, null, params);
        responseJson = gson.fromJson(body, Map.class);
        if (!responseJson.containsKey("name")) {
            return null;
        }
        AuthUserInfo authUserInfo = new AuthUserInfo();
        authUserInfo.setUsername(auth.getUsername());
        authUserInfo.setName(responseJson.get("name").toString());
        authUserInfo.setGenderWithString(responseJson.get("gender").toString());
        authUserInfo.setStatus(UserStatus.valueOf(PLATFORM.toUpperCase())
                .ordinal());
        try {
            authUserInfo
                    .setFaviconUrl(QiniuUploadUtils.uploadFavicon(auth
                            .getUsername(), responseJson.get("avatar_large")
                            .toString()));
        } catch (Exception e) {
            log.error(e);
            return null;
        }
        return authUserInfo;
    }
}