package room107.service.oauth;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.stereotype.Service;

import com.google.gson.Gson;

import room107.datamodel.GenderType;
import room107.datamodel.UserStatus;
import room107.util.QiniuUploadUtils;
import room107.util.WebUtils;

@CommonsLog
@Service
public class DoubanPlatform extends OauthPlatformImpl {
    private Gson gson = new Gson();

    private static final String PLATFORM = "douban";
    private static final String AUTH_URL = "https://www.douban.com/service/auth2/auth";
    private static final String TOKEN_URL = "https://www.douban.com/service/auth2/token";
    private static final String INFO_URL = "https://api.douban.com/v2/user/~me";
    private static final String CLIENT_ID = "0ac8dc20b45866d8205a94621d093488";
    private static final String CLIENT_SECRET = "5b9d0ab8e04ca7ab";
    private static final String REDIRECT_URI = "http://107room.com/user/oauth/douban/response";
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
        Map<String, String> authInfo = new HashMap<String, String>();
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
        auth.setUid(authInfo.get("douban_user_id"));
        auth.setUsernameWithUid(authInfo.get("douban_user_id").toString(),
                PLATFORM);
        return auth;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AuthUserInfo getUserInfo(Auth auth) {
        String body = null;
        Map<String, String> headers = new HashMap<String, String>();
        Map<String, ? super String> responseJson = null;

        headers.put("Authorization", "Bearer " + auth.getAccessToken());
        body = WebUtils.getWithHeaders(INFO_URL, headers, null);
        responseJson = gson.fromJson(body, Map.class);
        if (!responseJson.containsKey("uid")) {
            return null;
        }
        AuthUserInfo authUserInfo = new AuthUserInfo();
        authUserInfo.setUsername(auth.getUsername());
        authUserInfo.setName(responseJson.get("name").toString());
        authUserInfo.setGender(GenderType.UNKNOWN.ordinal());
        authUserInfo.setStatus(UserStatus.valueOf(PLATFORM.toUpperCase())
                .ordinal());
        try {
            authUserInfo
                    .setFaviconUrl(QiniuUploadUtils.uploadFavicon(auth
                            .getUsername(), responseJson.get("large_avatar")
                            .toString()));
        } catch (Exception e) {
            log.error(e);
            return null;
        }
        return authUserInfo;
    }
}
