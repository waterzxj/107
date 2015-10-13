package room107.aop;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import room107.util.web.DebugUtils;

/**
 * @author WangXiao
 */
@CommonsLog
public class DebugInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith("/static")) {
            return true;
        }
        DebugUtils.setDebugEnabled(DebugUtils.isDebugEnabled(request));
        if (log.isDebugEnabled()) {
            log.debug("Debug enabled: " + DebugUtils.isDebugEnabled());
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (modelAndView == null || !DebugUtils.isDebugEnabled()) {
            return;
        }
        Map<String, Object> model = modelAndView.getModel();
        modelAndView.addObject(DebugUtils.DEBUG_PARAM, true);
        // handle debug details
        if (DebugUtils.isShowDebugDetail(request)) {
            StringBuilder builder = new StringBuilder(
                    "<style>table * {border: 1px solid;}</style><table><tbody>");
            builder.append("<tr><td>view</td><td>")
                    .append(modelAndView.getViewName()).append("</td></tr>");
            builder.append("<tr><td>model</td></tr>");
            for (String key : model.keySet()) {
                builder.append("<tr><td>").append(key).append("</td><td>")
                        .append(model.get(key)).append("</td></tr>");
            }
            String modelString = builder.append("</tbody></table>").toString();
            if (log.isDebugEnabled()) {
                log.debug("Debug enabled: modelString=" + modelString);
            }
            modelAndView.addObject(DebugUtils.DEBUG_DETAIL_PARAM, modelString);
        }
    }

}
