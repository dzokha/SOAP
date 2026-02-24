package vn.dzokha.FastQC.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.annotation.PostConstruct;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Kích hoạt chế độ Headless của Java AWT.
     * Cực kỳ quan trọng để FastQC có thể vẽ biểu đồ (Graphics2D) trên môi trường Server/Docker.
     */
    @PostConstruct
    public void init() {
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map root URL vào index.html của Single Page Application (React/Vue)
        registry.addViewController("/").setViewName("forward:/index.html");
    }

    /**
     * Cấu hình CORS: Cho phép Frontend (ví dụ chạy ở port 3000) truy cập API của Backend.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*") // Trong sản xuất nên giới hạn domain cụ thể
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * Quản lý tài nguyên tĩnh và các báo cáo tạm thời.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Phục vụ các file trong thư mục static
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");

        // Nếu bạn lưu báo cáo HTML ra một thư mục tạm, hãy map nó ở đây để truy cập qua URL
        registry.addResourceHandler("/reports/**")
                .addResourceLocations("file:temp/reports/");
    }
}