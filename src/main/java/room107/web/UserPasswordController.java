package room107.web;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import room107.datamodel.User;
import room107.datamodel.web.JsonResponse;
import room107.service.user.IUserService;
import room107.util.JsonUtils;
import room107.util.UserBehaviorLog;
import room107.util.UserUtils;
import room107.util.WebUtils;
import room107.web.data.url.ResetPassword;

/**
 * @author WangXiao
 */
@CommonsLog
@Controller
@RequestMapping("/user")
public class UserPasswordController {
    
    private static final long RESET_PASSWORD_OUT_OF_DATE_TIME = 1000L * 60 * 15;

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "/auth/reset/send/")
    @ResponseBody
    public String resetSend(HttpServletRequest request,
            HttpServletResponse response, @RequestParam String userKey) {
        UserBehaviorLog.AUTH.info("Reset send: cookieId="
                + UserUtils.getCookieId(request, response) + ", IP="
                + WebUtils.getRealIp(request) + ", userKey=" + userKey);
        try {
            String email = userService.sendResetEmail(userKey);
            if (email == null) {
                return JsonResponse.NOT_FOUND;
            } else if (email.isEmpty()) {
                return JsonResponse.NO_CONTENT;
            } else {
                return new JsonResponse(email).toString();
            }
        } catch (Exception e) {
            log.error("Send reset email failed: userKey=" + userKey, e);
            return JsonResponse.error(e);
        }
    }

    @RequestMapping(value = "/auth/reset/verify/{data}")
    public String resetVerify(HttpServletRequest request,
            HttpServletResponse response, @PathVariable String data,
            Map<String, Object> map) {
        UserBehaviorLog.AUTH.info("Reset verify: cookieId="
                + UserUtils.getCookieId(request, response) + ", IP="
                + WebUtils.getRealIp(request) + ", data=" + data);
        try {
            ResetPassword resetPassword = JsonUtils.decrypt(data,
                    ResetPassword.class);
            /*
             * login
             */
            String username = resetPassword.getUsername();
            User user = userService.getUser(username);
            if (user == null ||
                    System.currentTimeMillis() - resetPassword.getTimestamp() > RESET_PASSWORD_OUT_OF_DATE_TIME) {
                map.put("code", HttpServletResponse.SC_GONE);
                return "error";
            }
            UserUtils.login(user, userService.getWxUserByUsername(username),
                    request, response);
        } catch (Exception e) {
            log.error("Reset verify failed: data=" + data, e);
            map.put("code", HttpServletResponse.SC_NOT_FOUND);
            return "error";
        }
        return "user-reset";
    }

    @RequestMapping(value = "/auth/reset/", method = RequestMethod.POST)
    @ResponseBody
    public String reset(HttpServletRequest request, HttpServletResponse response) {
        User user = UserUtils.getUser(request, response);
        if (user == null) {
            return JsonResponse.NOT_LOGGED_IN;
        }
        String password = request.getParameter("password");
        UserBehaviorLog.AUTH.info("Reset: "
                + UserUtils.getUserSignature(request, response));
        user = userService.getUser(user.getUsername());
        user.setPassword(UserUtils.encryptPassword(password));
        userService.updateUser(user);
        UserUtils.updateUser(request, response, user);
        return JsonResponse.OK;
    }

}
