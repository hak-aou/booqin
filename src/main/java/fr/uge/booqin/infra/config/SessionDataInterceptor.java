package fr.uge.booqin.infra.config;

import fr.uge.booqin.app.controller.mvc.session.SessionData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class SessionDataInterceptor implements HandlerInterceptor {

    private final SessionData sessionData;

    public SessionDataInterceptor(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView != null) {
            modelAndView.addObject("session", sessionData);
        }
    }
}