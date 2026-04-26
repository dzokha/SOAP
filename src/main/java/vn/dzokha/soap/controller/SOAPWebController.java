package vn.dzokha.soap.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.dzokha.soap.mapper.BasicStatsMapper;
import vn.dzokha.soap.dto.BasicStatsDTO;
import vn.dzokha.soap.dto.AnalysisReportDTO; // Fix lỗi không thấy DTO
import vn.dzokha.soap.dto.TileDataDTO; // FIX LỖI: Import DTO quan trọng này
import vn.dzokha.soap.dto.BoxPlotPointDTO;

import vn.dzokha.soap.model.AnalysisResult;
import vn.dzokha.soap.service.AnalysisService;
import vn.dzokha.soap.engine.shared.PerTileQualityScores;
import vn.dzokha.soap.engine.shared.PerBaseQualityScores;
import vn.dzokha.soap.engine.adapter.AdapterContent; // Để dùng được trong instanceof

import vn.dzokha.soap.util.RawDataViewer;


import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@RestController
@RequestMapping("/api/v1/soap")
@Tag(name = "SOAP Analysis", description = "Các API liên quan đến phân tích chất lượng trình tự DNA")
public class SOAPWebController {

    private static final Logger log = LoggerFactory.getLogger(SOAPWebController.class);

    private final AnalysisService analysisService;
    private final BasicStatsMapper basicStatsMapper;

    private final RawDataViewer rawDataViewer;

    public SOAPWebController(AnalysisService analysisService, BasicStatsMapper basicStatsMapper, RawDataViewer rawDataViewer) {
        this.analysisService = analysisService;
        this.basicStatsMapper = basicStatsMapper;
        this.rawDataViewer = rawDataViewer;
    }

    /* 
    * Máy chủ Tomcat của Spring Boot có khoảng 200 luồng (threads) để tiếp khách. 
    * Nếu 200 người cùng upload file DNA khổng lồ và chờ xử lý tuần tự, server sẽ hết luồng và sập
    * CompletableFuture: Đây là tính năng lập trình đa luồng (Concurrency) hiện đại của Java.
    * Tomcat nhận file xong sẽ lập tức ném nhiệm vụ này cho một luồng chạy nền (Background thread) 
    * do analysisService.processMultipleFiles quản lý.
    * Luồng Tomcat lập tức được giải phóng để đi đón khách khác.
    * thenApply(...): Khi nào luồng chạy nền phân tích xong, nó sẽ tự động chạy vào hàm này để đóng gói kết quả 
    * (ResponseEntity.ok) trả về cho Frontend.
    */
    @PostMapping(value = "/analyze", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<List<AnalysisReportDTO>>> analyze(@RequestParam("files") MultipartFile[] files) {
        log.info("Hệ thống đang xử lý {} file trình tự.", files.length);
        return  analysisService.processMultipleFiles(files).thenApply(results -> {
                    List<AnalysisReportDTO> dtos = results.stream().map(this::convertToDTO).toList();
                    log.info("Đã phân tích xong và đóng gói kết quả cho {} file.", dtos.size());
                    return ResponseEntity.ok(dtos);
                }).exceptionally(ex -> {
                    log.error("Lỗi phân tích: {}", ex.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                });
    }


    @GetMapping("/per-tile-quality")
    public ResponseEntity<TileDataDTO> getPerTileQuality() {
        try {
            PerTileQualityScores module = analysisService.getModule(PerTileQualityScores.class);
            if (module == null) return ResponseEntity.noContent().build();
            return ResponseEntity.ok(module.getChartData());
        } catch (Exception e) {
            log.error("Không thể lấy dữ liệu Tile Quality: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Mapping từ Backend (AnalysisResult) tới (AnalysisReportDTO) gửi cho Frontend 
     */

    private AnalysisReportDTO convertToDTO(AnalysisResult result) {
        AnalysisReportDTO dto = new AnalysisReportDTO();
        dto.setFileName(result.getFileName());
        dto.setStatus(result.getStatus());
        
        if (result.getModules() != null) {
            for (Object module : result.getModules()) {
                
                // 1. Lấy dữ liệu Adapter
                if (module instanceof AdapterContent adapter) {
                    dto.setAdapterData(adapter.getAdapterContentData());
                }
                
                // 2. THÊM MỚI: Lấy dữ liệu Per Base Quality
                // Lưu ý: Đổi "PerBaseQualityScores" thành tên class module thực tế của bạn
                else if (module instanceof PerBaseQualityScores qualityModule) {
                    // Giả sử qualityModule.getBoxPlotData() trả về List<BoxPlotPointDTO>
                    dto.setPerBaseQualityData(qualityModule.getBoxPlotData()); 
                }
            }
        }
        return dto;
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("SOAP Analysis Service is active (Java 21/25).");
    }


    @PostMapping(value = "/view-raw", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> viewRawData(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        
        log.info("Yêu cầu xem dữ liệu thô cho file: {}, giới hạn: {} đoạn", file.getOriginalFilename(), limit);

        try {
            // SỬA Ở ĐÂY: Gọi hàm qua instance (object) thay vì gọi static class
            List<String> rawLines = rawDataViewer.getRawFastqFromStream(file.getInputStream(), limit);
            
            String result = String.join("\n", rawLines);
            if (rawLines.isEmpty()) {
                return ResponseEntity.ok("File không có dữ liệu hoặc định dạng không đúng.");
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Lỗi khi đọc dữ liệu thô: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể đọc file: " + e.getMessage());
        }
    }
}