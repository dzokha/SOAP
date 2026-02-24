package vn.dzokha.FastQC.Config;

import org.springframework.stereotype.Component;
import java.io.File;

@Component // Đánh dấu để Spring quản lý, thay thế cho Singleton getInstance()
public class FastQCConfig {
    
    // Các tham số cấu hình (Chuyển sang private để đảm bảo tính đóng gói)
    public boolean nogroup = false;
    public boolean expgroup = false;
    private boolean quiet = false;
    private boolean show_version = false;
    public Integer kmer_size = 7;
    private Integer threads = null;
    private boolean showUpdates = true;
    private File output_dir = null;
    private boolean casava = false;
    private boolean nano = false;
    private boolean nofilter = false;
    public Boolean do_unzip = null;
    private boolean delete_after_unzip = false;
    private String lineSeparator = System.getProperty("line.separator");
    private String sequence_format = null;
    public File contaminant_file = null;
    public File adapter_file = null;
    public File limits_file = null;
    public int minLength = 50;
    public int dupLength = 50;
    private boolean svg_output = false;

    // Constructor mặc định cho Spring
    public FastQCConfig() {
        // Có thể nạp các giá trị mặc định từ System.getProperty nếu chạy CLI
        loadFromSystemProperties();
    }

    private void loadFromSystemProperties() {
        if (System.getProperty("fastqc.casava") != null) {
            this.casava = Boolean.parseBoolean(System.getProperty("fastqc.casava"));
        }
        if (System.getProperty("fastqc.nofilter") != null) {
            this.nofilter = Boolean.parseBoolean(System.getProperty("fastqc.nofilter"));
        }
        if (System.getProperty("fastqc.sequence_format") != null) {
            this.sequence_format = System.getProperty("fastqc.sequence_format");
        }
        // Thêm các logic nạp property khác tương tự...
    }

    // --- CÁC GETTER QUAN TRỌNG ĐỂ FIX LỖI BIÊN DỊCH ---

    public boolean isCasava() {
        return casava;
    }

    public boolean isNofilter() {
        return nofilter;
    }

    public String getSequenceFormat() {
        return sequence_format;
    }

    // --- CÁC GETTER/SETTER KHÁC (Để code sạch hơn) ---

    public void setSequenceFormat(String format) {
        if (format == null || format.matches("fastq|sam|bam|sam_mapped|bam_mapped")) {
            this.sequence_format = format;
        } else {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
    }

    private static FastQCConfig instance = new FastQCConfig();
    public static FastQCConfig getInstance() { return instance; }

    public void setCasava(boolean casava) { this.casava = casava; }
    public void setNofilter(boolean nofilter) { this.nofilter = nofilter; }
    public int getKmerSize() { return kmer_size != null ? kmer_size : 7; }
    public int getThreads() { return threads != null ? threads : 1; }
    public String getLineSeparator() { return lineSeparator; }
}