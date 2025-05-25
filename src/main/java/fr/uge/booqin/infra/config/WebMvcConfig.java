package fr.uge.booqin.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final SessionDataInterceptor sessionDataInterceptor;

    public WebMvcConfig(SessionDataInterceptor sessionDataInterceptor) {
        this.sessionDataInterceptor = sessionDataInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionDataInterceptor);
    }
}