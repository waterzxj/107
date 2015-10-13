package room107.service.oauth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.apachecommons.CommonsLog;

import com.google.gson.Gson;

import room107.datamodel.UserStatus;
import room107.util.QiniuUploadUtils;
import room107.util.WebUtils;

@CommonsLog
@Service
public class QQPlatform extends OauthPlatformImpl {
    private Gson gson = new Gson();

    private static final String PLATFORM = "qq";
    private static final String AUTH_URL = "https://graph.qq.com/oauth2.0/authorize";
    private static final String TOKEN_URL = "https://graph.qq.com/oauth2.0/token";
    private static final String OPEN_ID_URL = "https://graph.qq.com/oauth2.0/me";
    private static final String INFO_URL = "https://graph.qq.com/user/get_user_info";
    private static final String CLIENT_ID = "101215955";
    private static final String CLIENT_SECRET = "7ff1ca87728d934ae049961540ff5f9c";
    private static final String REDIRECT_URI = "http://107room.com/user/oauth/qq/response";
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

    @Override
    public Auth getAuth(String code) {
        String body = "";
        String accessToken = null;
        String openId = null;
        Map<String, String> options = new HashMap<String, String>();
        options.put("client_id", CLIENT_ID);
        options.put("client_secret", CLIENT_SECRET);
        options.put("redirect_uri", REDIRECT_URI);
        options.put("grant_type", GRANT_TYPE);
        options.put("code", code);

        body = WebUtils.post(TOKEN_URL, options);
        accessToken = body.substring(body.indexOf('=') + 1, body.indexOf('&'));

        if (accessToken == null || accessToken.isEmpty()) {
            // access token doesn't exist
            return null;
        }
        openId = getOpenId(accessToken);
        Auth auth = new Auth();
        auth.setAccessToken(accessToken);
        auth.setUid(openId);
        auth.setUsernameWithUid(openId, PLATFORM);
        return auth;
    }

    @SuppressWarnings("unchecked")
    @Override
    public AuthUserInfo getUserInfo(Auth auth) {
        String body = null;
        Map<String, String> params = new HashMap<String, String>();
        Map<String, ? super String> responseJson = null;

        params.put("access_token", auth.getAccessToken());
        params.put("oauth_consumer_key", CLIENT_ID);
        params.put("openid", auth.getUid());
        body = WebUtils.getWithHeaders(INFO_URL, null, params);
        responseJson = gson.fromJson(body, Map.class);
        if (!responseJson.containsKey("nickname")) {
            return null;
        }
        AuthUserInfo authUserInfo = new AuthUserInfo();
        authUserInfo.setName(responseJson.get("nickname").toString());
        authUserInfo.setUsername(auth.getUsername());
        authUserInfo.setGenderWithString(responseJson.get("gender").toString());
        authUserInfo.setStatus(UserStatus.valueOf(PLATFORM.toUpperCase())
                .ordinal());
        try {
            authUserInfo
                    .setFaviconUrl(QiniuUploadUtils.uploadFavicon(auth
                            .getUsername(), responseJson.get("figureurl_2")
                            .toString()));
        } catch (Exception e) {
            log.error(e);
            return null;
        }
        return authUserInfo;
    }

    public String getOpenId(String accessToken) {
        String qqOpenId = "";
        Map<String, String> params = new HashMap<String, String>();
        params.put("access_token", accessToken);
        qqOpenId = WebUtils.getWithHeaders(OPEN_ID_URL, null, params);
        log.info("OPENIDBODY");
        log.info(qqOpenId);
        qqOpenId = qqOpenId.substring(qqOpenId.indexOf("openid") + 9,
                qqOpenId.indexOf("\"}"));
        return qqOpenId;
    }
}