package room107.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import room107.web.session.SessionKeys;
import room107.web.session.SessionManager;

/**
 * @author WangXiao
 */
public class AdminInterceptor extends HandlerInterceptorAdapter {
    
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().contains("/admin/user/login")) {
            return super.preHandle(request, response, handler);
        }
        // validate
        if (SessionManager.getSessionValue(request, response, SessionKeys.ADMIN) == null) {
            response.sendRedirect("/admin/user/login");
            return false;
        }
        return true;
    }

}
