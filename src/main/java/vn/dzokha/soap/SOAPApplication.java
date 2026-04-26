package vn.dzokha.soap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; 

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Paths;

import vn.dzokha.soap.config.SOAPProperties;

@SpringBootApplication
@EnableAsync 
public class SOAPApplication {
    public static void main(String[] args) {
        SpringApplication.run(SOAPApplication.class, args);
    }

    @Bean
    CommandLineRunner init(SOAPProperties props) {
        return args -> {
            // Tự động tạo thư mục khi ứng dụng khởi động thành công
            Files.createDirectories(Paths.get(props.getUploadDir()));
            System.out.println(">>> Hệ thống đã sẵn sàng. Thư mục dữ liệu đã được khởi tạo.");
        };
    }
}