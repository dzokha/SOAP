package vn.dzokha.FastQC.Config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fastqc") // Tiền tố trong file yml
public class FastQCProperties {

    private String uploadDir = "uploads/"; // Giá trị mặc định
    private long maxFileSize = 100 * 1024 * 1024; // 100MB
    private int threadCount = 4;
    private Logging logging = new Logging();

    // Getter và Setter (Bắt buộc phải có để Spring nạp dữ liệu)
    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }

    public long getMaxFileSize() { return maxFileSize; }
    public void setMaxFileSize(long maxFileSize) { this.maxFileSize = maxFileSize; }

    public int getThreadCount() { return threadCount; }
    public void setThreadCount(int threadCount) { this.threadCount = threadCount; }

    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }

    // Nested class cho các cấu hình sâu hơn
    public static class Logging {
        private String level = "INFO";
        private String path = "logs/";

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}