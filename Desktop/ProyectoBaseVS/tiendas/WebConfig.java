package com.proyecto.tiendas;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final PermisosInterceptor permisos;

    public WebConfig(PermisosInterceptor permisos) { this.permisos = permisos; }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(permisos).addPathPatterns("/api/**");
    }
}