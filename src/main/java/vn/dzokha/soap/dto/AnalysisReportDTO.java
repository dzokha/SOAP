package vn.dzokha.soap.dto;

import java.util.List;
import java.util.ArrayList;

public class AnalysisReportDTO {
    private String fileName;
    private String status;
    
    // Dữ liệu cho biểu đồ Adapter Content
    private List<?> adapterData = new ArrayList<>();
    
    // THÊM MỚI: Dữ liệu cho biểu đồ Per Base Sequence Quality
    private List<?> perBaseQualityData = new ArrayList<>();

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<?> getAdapterData() { return adapterData; }
    public void setAdapterData(List<?> adapterData) { this.adapterData = adapterData; }

    // THÊM MỚI: Getter / Setter cho Quality Data
    public List<?> getPerBaseQualityData() { return perBaseQualityData; }
    public void setPerBaseQualityData(List<?> perBaseQualityData) { this.perBaseQualityData = perBaseQualityData; }
}