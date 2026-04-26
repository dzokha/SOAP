package vn.dzokha.soap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.PostConstruct;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @PostConstruct
    public void init() {
        // Cần thiết cho xử lý ảnh/biểu đồ trên Linux/Docker
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Cho phép Thymeleaf quản lý trang chủ
        registry.addViewController("/").setViewName("index");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Tài nguyên tĩnh trong dự án
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Tài nguyên báo cáo xuất ra bên ngoài
        registry.addResourceHandler("/reports/**")
                .addResourceLocations("file:temp/reports/");
    }
}