package room107.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mobile.device.site.SitePreference;
import org.springframework.mobile.device.site.SitePreferenceUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.dao.UsernameExistException;
import room107.datamodel.GenderType;
import room107.datamodel.LogType;
import room107.datamodel.RegRole;
import room107.datamodel.User;
import room107.datamodel.UserStatus;
import room107.datamodel.UserType;
import room107.datamodel.VerifyEmailStatus;
import room107.datamodel.WxUser;
import room107.datamodel.web.JsonResponse;
import room107.datamodel.web.UserInfo;
import room107.service.api.weixin.WeiXinService;
import room107.service.oauth.IOauthPlatform.ReleaseStatus;
import room107.service.oauth.IOauthService;
import room107.service.user.IUserService;
import room107.service.user.IVerifyService;
import room107.service.user.IVerifyService.ConfirmData;
import room107.service.user.IVerifyService.ConfirmSource;
import room107.util.EncryptUtils;
import room107.util.JsonUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserStatLog;
import room107.util.UserUtils;
import room107.util.ValidateUtils;
import room107.util.WebUtils;
import room107.web.session.SessionManager;

import com.google.gson.Gson;

/**
 * @author WangXiao
 * @author OYYD
 */
@CommonsLog
@Controller
@RequestMapping("/user")
@SuppressWarnings("unchecked")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private WeiXinService weiXinService;

    @Autowired
    private IVerifyService verifyService;
    
    @Autowired
    private IOauthService oauthService;
    
    public static String COOKIE_LOGIN_USERNAME = "LOGIN_USERNAME";
    
    @RequestMapping(value = "/feedback")
    public String feedback(HttpServletRequest request, Map<String, Object> map) {
        return "user-feedback";
    }

    @RequestMapping(value = "/exist/{username}")
    @ResponseBody
    public String existJson(HttpServletRequest request,
            @PathVariable String username, Map<String, Object> map) {
        boolean exist = username != null
                && userService.getUser(username) != null;
        return JsonUtils.toJson("exist", exist);
    }

    @RequestMapping(value = "/auth/register")
    public String register(HttpServletRequest request, Map<String, Object> map) {
        return "user-auth";
    }
    
    @RequestMapping(value = "/auth/register/submit", method = RequestMethod.POST)
    @ResponseBody
    public String registerSubmit(HttpServletRequest request,
            HttpServletResponse response, @RequestParam String gender,
            @RequestParam String username, @RequestParam String password,
            @RequestParam(defaultValue = "1") int role,
            @RequestParam(required = false) String euid) throws Exception {
        UserBehaviorLog.BASIC.info("Register: usename=" + username
                + ", regRole=" + role);
        /*
         * validate
         */
        List<String> invalidInputs = new ArrayList<String>();
        if (!ValidateUtils.validateEmail(username)) {
            invalidInputs.add("username");
        }
        if (!ValidateUtils.validatePassword(password)) {
            invalidInputs.add("password");
        }
        if (!ValidateUtils.validateGender(gender)) {
            invalidInputs.add("gender");
        }
        /*
         * log on if valid
         */
        if (invalidInputs.isEmpty()) { // valid
            // create new user
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(username);
            user.setGender(WebUtils.getInt(request, gender,
                    GenderType.UNKNOWN.ordinal()));
            user.setType(UserType.TENANT.ordinal());
            user.setRegRole(role);
            // login
            try {
                user = userService.createUser(user);
                // from weixin
                WxUser wxUser = null;
                if (euid != null) {
                    String openId = EncryptUtils.decrypt(euid);
                    wxUser = weiXinService.bind(openId, username);
                }
                UserUtils.login(user, wxUser, request, response);
                UserStatLog.log(request, response, LogType.register);
                return JsonUtils.toJson(user);
            } catch (UsernameExistException e) {
                invalidInputs.add("username.exist");
            }
        }
        return JsonUtils.toJson(invalidInputs);
    }

    @RequestMapping(value = "/auth/verify")
    public String verify(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        User user = UserUtils.getUser(request, response);
        String token = null;
        if(user != null){
            token = user.getToken();
        }
        String verifyMethod = request.getParameter("method");
        if (verifyMethod == null) {
            verifyMethod = "email";
        }
        map.put("verifyMethod", verifyMethod);
        map.put("token", token);
        return "user-auth";
    }
    
	private String handleOauthReturn(Map<String, Object> resMap, 
			boolean isSuccess, String cookieId, User user){
    	if(!isSuccess){
    		resMap.put("invalid", true);
    	}
    	oauthService.confirmOauthStatus(cookieId, user);
    	return "oauth-end";
    };
    
	@RequestMapping(value="/oauth/{platform}/response")
    public String getOauthInfo(HttpServletRequest request, 
    		HttpServletResponse response, Map<String, Object> map,
    		@PathVariable String platform){
    	String error = request.getParameter("error");
    	String code = request.getParameter("code");    	
    	String cookieId = UserUtils.getCookieId(request, response);
    	User user = null;    	
    	
    	//check request
    	if((error != null && !error.isEmpty()) ||(code == null || code.isEmpty())){
    		return handleOauthReturn(map, false, cookieId, null);
    	}
    	user = oauthService.getOauthUser(platform, code);
    	if(user == null){
    		return handleOauthReturn(map, false, cookieId, null);
    	}
        UserUtils.login(user, null, request, response);
        UserStatLog.log(request, response, LogType.register);
    	
    	return handleOauthReturn(map, true, cookieId, user);
    }

    @RequestMapping(value = "/auth/verify/email", method = RequestMethod.POST)
    @ResponseBody
    public String verifyEmail(HttpServletRequest request, HttpServletResponse response) {
        String username = UserUtils.getUsername(request, response);
        if (username == null) {
            return "-1";
        }
        String email = request.getParameter("email");
        int source = WebUtils.getInt(request, "source", 0);
        VerifyEmailStatus result = verifyService.verifyEmail(new ConfirmData(
                username, email, ConfirmSource.values()[source]));
        return String.valueOf(result.ordinal());
    }

    @RequestMapping(value = "/qrcode")
    public String wechat(HttpServletRequest request, Map<String, Object> map) {
        return "user-qrcode";
    }

    @RequestMapping(value = "/login")
    public String login(HttpServletRequest request, Map<String, Object> map) {
        String lastLoginUsername = WebUtils.getCookieValue(request,
                COOKIE_LOGIN_USERNAME);
        if (lastLoginUsername != null) {
            map.put("lastLoginUsername", lastLoginUsername);
        }
        map.put("loginUrls",oauthService.getLoginUrls());
        return "user-login";
    }

    /**
     * Check the login status of a user. Especially useful for
     * those pages which would login or check account status but
     * don't want to redirect.
     * @param request
     * @return user or null 
     */
    @RequestMapping(value = "/login/check", method = RequestMethod.GET)
    @ResponseBody
    public String isLogin(HttpServletRequest request,
    		HttpServletResponse response){    	
    	boolean hasCookieId = UserUtils.containCookieId(request, response);
    	// this will also set cookie if not set
    	String cookieId = UserUtils.getCookieId(request, response);
    	ReleaseStatus releaseStatus = null;
    	Map<String, Object> map = new HashMap<String, Object>();

    	// return and set "CID" cookie field if user agent(browser) do not have.
    	if(!hasCookieId){
    		map.put("isReturned", false);
    		return JsonUtils.toJson(map);
    	}
    	releaseStatus = oauthService.waitOauthStatus(cookieId);
    	return JsonUtils.toJson(releaseStatus);
    }

    @RequestMapping(value = "/login/submit", method = RequestMethod.POST)
    @ResponseBody
    public String loginSubmit(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map,
            @RequestParam(required = false) String euid) throws Exception {
        String username = StringUtils.trimToNull(request
                .getParameter("username"));
        String password = StringUtils.trimToNull(request
                .getParameter("password"));
        if (username != null && password != null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user = userService.login(user);
            if (user != null) { // login successfully
                UserUtils.login(user, userService.getWxUserByUsername(username), 
                        request, response);
                if (euid != null) {
                    weiXinService.bind(EncryptUtils.decrypt(euid), username);
                }
                UserStatLog.log(request, response, LogType.login);
                return JsonUtils.toJson(user);
            }
        }
        // invalid
        if (log.isDebugEnabled()) {
            log.debug("Invliad: username=" + username + ", password="
                    + password);
        }
        return null;
    }

    /**
     * Update user info in session.
     * 
     * @return latest user or null when not logged in
     */
    @RequestMapping(value = "/update")
    @ResponseBody
    public String update(HttpServletRequest request, HttpServletResponse response, Map<String, Object> map) {
        String username = UserUtils.getUsername(request, response);
        if (username == null) { // not logged in
            return null;
        }
        User user = userService.getUser(username);
        UserUtils.updateUser(request, response, user);
        return JsonUtils.toJson(user);
    }

    @RequestMapping(value = "/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        UserBehaviorLog.BASIC.info("Logout: username="
                + UserUtils.getUsername(request, response));
        SessionManager.removeSession(request, response);
        return "redirect:/";
    }

    @RequestMapping(value = "/unbind")
    @ResponseBody
    public String apiLogout(HttpServletRequest request, HttpServletResponse response) {
        UserBehaviorLog.BASIC.info("Unbind: username="
                + UserUtils.getUsername(request, response));
        WxUser wxUser = UserUtils.getWxUser(request, response);
        userService.deleteWxUser(wxUser);
        SessionManager.removeSession(request, response);
        return null;
    }

    @RequestMapping(value = "/k{key}")
    public String grant(HttpServletRequest request, Map<String, Object> map, @PathVariable String key) {
        User user = userService.getEncryptUser(key);
        if (user != null) {
            if (user.getStatus() == UserStatus.ANONYMOUS.ordinal()) {
                UserBehaviorLog.AUTH.info("user grant request, key = " + key + ", username = " + user.getUsername());
                map.put("key", key);
                if(isMobile(request)){
                	return "mobile/user-grant";
                }
                return "user-grant";
            } else {
                UserBehaviorLog.AUTH.info("user grant request, key = " + key + ", username = " + user.getUsername() + ", redirect to user-login.");
                return "redirect:/user/login";
            }
        }
        UserBehaviorLog.AUTH.info("user grant request failed, invalid key = " + key);
        map.put("code", 404);
        return "error";
    }
    
    @RequestMapping(value = "/grant/submit")
    @ResponseBody
    public String grantSubmit(HttpServletRequest request,
            HttpServletResponse response, Map<String, Object> map) {
        String key = WebUtils.getNullIfEmpty(request, "key");
        String username = WebUtils.getNullIfEmpty(request, "username");
        String password = WebUtils.getNullIfEmpty(request, "password");
        String gender = WebUtils.getNullIfEmpty(request, "gender");
        int role = WebUtils.getInt(request, "role", RegRole.PC_LANDLORD);
        
        User anonymousUser = userService.getEncryptUser(key);
        if (anonymousUser == null) {
            UserBehaviorLog.BASIC.info("user grant failed: key=" + key + ", usename=" + username);
            map.put("code", 404);
            return "error";
        }
        
        /*
         * validate
         */
        List<String> invalidInputs = new ArrayList<String>();
        if (!ValidateUtils.validateEmail(username)) {
            invalidInputs.add("username");
        }
        if (!ValidateUtils.validatePassword(password)) {
            invalidInputs.add("password");
        }
        if (!ValidateUtils.validateGender(gender)) {
            invalidInputs.add("gender");
        }
        if (invalidInputs.isEmpty()) { // valid
            // create new user
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setEmail(username);
            user.setGender(WebUtils.getInt(request, gender,
                    GenderType.UNKNOWN.ordinal()));
            user.setRegRole(role);
            // login
            try {
                user = userService.grantUser(anonymousUser, user);
                UserBehaviorLog.BASIC.info("user grant success: key=" + key + ", username=" + username);
                UserUtils.login(user, null, request, response);
                UserStatLog.log(request, response,
                        LogType.grant, JsonUtils.toJson("key", key, "origin", anonymousUser.getUsername()));
                return JsonUtils.toJson(user);
            } catch (UsernameExistException e) {
                invalidInputs.add("username.exist");
            }
        }
        return JsonUtils.toJson(invalidInputs);
    }

    @RequestMapping(value = "/updateInfo", method = RequestMethod.POST)
    @ResponseBody
    public String updateUser(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        Gson gson = new Gson();
        // house
        String json = request.getParameter("userinfo");
        if (json != null) {
            Map<String, ?> map = gson.fromJson(json, Map.class);
            if (log.isDebugEnabled()) {
                log.debug("Deserialized JSON: userinfo=" + map);
            }
            UserInfo userInfo = deserialize(map, new UserInfo());
            if (StringUtils.isEmpty(userInfo.getUsername())) {
                userInfo.setUsername(user.getUsername());
            }
            UserBehaviorLog.BASIC.info("update user info: " + userInfo);
            user = userService.updateInfo(userInfo.toUser());
            UserUtils.updateUser(request, response, user);
        }
        return JsonResponse.OK;
    }

    private boolean isValid(Object value) {
        return value != null && (!value.toString().trim().isEmpty());
    }

    private UserInfo deserialize(Map<String, ?> map, UserInfo user) {
        if (map == null) {
            return null;
        }
        Object value = map.get("username");
        if (isValid(value)) {
            user.setUsername(value.toString().trim());
        }
        value = map.get("name");
        if (isValid(value)) {
            user.setName(value.toString().trim());
        }
        value = map.get("age");
        if (isValid(value)) {
            user.setAge((int) Double.parseDouble(value.toString()));
        }
        value = map.get("gender");
        if (isValid(value)) {
            user.setGender(value.toString().trim());
        }
        value = map.get("constellation");
        if (isValid(value)) {
            user.setConstellation(value.toString().trim());
        }
        value = map.get("school");
        if (isValid(value)) {
            user.setSchool(value.toString().trim());
        }
        value = map.get("major");
        if (isValid(value)) {
            user.setMajor(value.toString().trim());
        }
        value = map.get("company");
        if (isValid(value)) {
            user.setCompany(value.toString().trim());
        }
        value = map.get("profession");
        if (isValid(value)) {
            user.setProfession(value.toString().trim());
        }
        value = map.get("detail");
        if (isValid(value)) {
            user.setDescription(value.toString().trim());
        }
        return user;
    }

    private Boolean isMobile(HttpServletRequest request){
    	SitePreference preference = SitePreferenceUtils.getCurrentSitePreference(request);
    	
    	if(preference.isMobile() || preference.isTablet()){
    		return true;
    	}
    	return false;
    }
}
