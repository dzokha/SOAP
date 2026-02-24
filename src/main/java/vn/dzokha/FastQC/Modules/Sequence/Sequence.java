package vn.dzokha.FastQC.Modules.Sequence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.io.File;

/**
 * Class Sequence đại diện cho một bản ghi Read trong file FASTQ.
 * Đã được chuẩn hóa để tương thích với các module phân tích và Web API.
 */
public class Sequence implements Serializable {

    private String id;
    private String sequence;
    private String quality;
    private String colorspace; // Dùng cho dữ liệu màu nếu có
    private boolean isFiltered = false;
    
    @JsonIgnore // Tránh lỗi vòng lặp hoặc serialize file object khi trả về Web API
    private File sourceFile;

    // Constructor chuẩn cho dữ liệu FASTQ
    public Sequence(String id, String sequence, String quality) {
        this.id = id;
        this.sequence = (sequence != null) ? sequence.toUpperCase() : "";
        this.quality = quality;
    }

    // Constructor hỗ trợ đầy đủ nguồn file và colorspace
    public Sequence(String id, String sequence, String quality, File sourceFile) {
        this(id, sequence, quality);
        this.sourceFile = sourceFile;
    }

    // --- CÁC PHƯƠNG THỨC GETTER ĐỂ FIX LỖI MAVEN ---

    public String getSequence() { 
        return sequence; 
    }

    // Fix lỗi BasicStats & PerBaseQuality: Yêu cầu hàm tên getQualityString
    public String getQualityString() { 
        return quality; 
    }

    // Fix lỗi PerTileQuality: Yêu cầu getID (viết hoa ID)
    public String getID() { 
        return id; 
    }

    // Fix lỗi BasicStats: Yêu cầu hàm file() trả về File object
    public File file() { 
        return sourceFile; 
    }

    // Fix lỗi so sánh trong BasicStats: Trả về boolean thay vì String
    public boolean getColorspace() { 
        return colorspace != null; 
    }

    // --- CÁC GETTER/SETTER BỔ TRỢ CHO WEB ---

    public String getId() { return id; }
    public String getQuality() { return quality; }
    public boolean isFiltered() { return isFiltered; }
    public void setFiltered(boolean filtered) { isFiltered = filtered; }
    public void setFile(File file) { this.sourceFile = file; }
}