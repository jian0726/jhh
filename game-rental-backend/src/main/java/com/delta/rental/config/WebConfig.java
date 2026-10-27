package com.delta.rental.config;

import com.delta.rental.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源映射。
 *
 * <p>其中 {@code /uploads/**} 指向 {@link FileStorageService} 使用的上传目录，
 * 保证"文件存到哪、就从哪读"，两边路径不会写歪。
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final FileStorageService fileStorageService;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");
        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        // 上传图片的访问入口：file:{uploadDir}
        registry.addResourceHandler(FileStorageService.URL_PREFIX + "**")
                .addResourceLocations("file:" + fileStorageService.getUploadDir());
    }

    // CORS 配置已统一到 SecurityConfig.corsConfigurationSource() 中
}
