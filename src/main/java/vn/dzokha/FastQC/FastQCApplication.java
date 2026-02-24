package vn.dzokha.FastQC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync; 

@SpringBootApplication
@EnableAsync 
public class FastQCApplication {
    public static void main(String[] args) {
        SpringApplication.run(FastQCApplication.class, args);
    }
}