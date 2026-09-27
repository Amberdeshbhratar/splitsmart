package com.amber.splitsmart.common;
import com.amber.splitsmart.auth.JwtRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final JwtRequestInterceptor jwt;
    private final String[] allowedOrigins;
    public WebConfig(JwtRequestInterceptor jwt, @Value("${app.cors.allowed-origins}") String origins) { this.jwt = jwt; this.allowedOrigins = origins.split(","); }
    @Override public void addCorsMappings(CorsRegistry registry) { registry.addMapping("/api/**").allowedOrigins(allowedOrigins).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS"); }
    @Override public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(jwt).addPathPatterns("/api/**").excludePathPatterns("/api/auth/**");
    }
}
