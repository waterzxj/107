package room107.aop;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import room107.util.WebUtils;

/**
 * @author WangXiao
 */
public class WebInterceptor extends HandlerInterceptorAdapter {

    private static final String IP = "115.28.42.203";

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        StringBuffer buffer = request.getRequestURL();
        int i = buffer.indexOf(IP);
        if (i >= 0) {
            buffer.replace(i, i + IP.length(), "107room.com");
            response.sendRedirect(buffer.toString());
            return false;
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            // set whether in embedded model
            WebUtils.checkEmbedded(request, modelAndView.getModel());
        }
    }

}
