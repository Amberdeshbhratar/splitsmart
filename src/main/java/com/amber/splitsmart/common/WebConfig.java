package com.amber.splitsmart.common;
import com.amber.splitsmart.auth.JwtRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtRequestInterceptor jwt;
    public WebConfig(JwtRequestInterceptor jwt) { this.jwt = jwt; }
    @Override public void addCorsMappings(CorsRegistry registry) { registry.addMapping("/api/**").allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173").allowedMethods("GET", "POST", "PUT", "DELETE"); }
    @Override public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(jwt).addPathPatterns("/api/**").excludePathPatterns("/api/auth/**");
    }
}
