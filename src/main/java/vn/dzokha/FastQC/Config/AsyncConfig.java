package vn.dzokha.FastQC.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "analysisTaskExecutor")
    public Executor analysisTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Số lượng "khe cắm" chạy đồng thời (tương đương threads trong config cũ)
        executor.setCorePoolSize(4); 
        executor.setMaxPoolSize(10);
        // Hàng đợi chờ nếu tất cả các khe đều đang bận
        executor.setQueueCapacity(100); 
        executor.setThreadNamePrefix("FastQC-Analysis-");
        executor.initialize();
        return executor;
    }
}