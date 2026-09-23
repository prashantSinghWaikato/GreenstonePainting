package org.greenstone.backend.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String allowedOrigin;

    public WebConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        addPage(registry, "admin");
        addPage(registry, "quote");
        addPage(registry, "services");
        addPage(registry, "projects");
        addPage(registry, "about");
        addPage(registry, "service-areas");
        addPage(registry, "contact");
        addPage(registry, "privacy");
        addPage(registry, "blog");
        addPage(registry, "how-painters-prepare-your-home-for-a-smooth-paint-job");
        addPage(registry, "822-2");
        addPage(registry, "wood-staining-benefits-you-need-to-take-advantage-of");
    }

    private void addPage(ViewControllerRegistry registry, String path) {
        var viewName = "forward:/" + path + "/index.html";
        registry.addViewController("/" + path).setViewName(viewName);
        registry.addViewController("/" + path + "/").setViewName(viewName);
    }
}
