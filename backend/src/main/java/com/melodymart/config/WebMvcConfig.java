package com.melodymart.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC Web Configuration.
 * Configures static resource handlers for local audio tracks and static assets.
 *
 * Physical file:
 * backend/src/main/resources/static/assets/audio/song1.mp3
 * Database:
 * AudioFileURL = /assets/audio/song1.mp3
 * Browser:
 * http://localhost:8080/assets/audio/song1.mp3
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /assets/** to classpath:/static/assets/
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");

        // Fallback for legacy /audio/** paths mapped to classpath:/static/assets/audio/
        registry.addResourceHandler("/audio/**")
                .addResourceLocations("classpath:/static/assets/audio/", "classpath:/static/audio/");
    }
}
