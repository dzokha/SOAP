package vn.dzokha.FastQC.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.dzokha.FastQC.Mappers.BasicStatsMapper;
import vn.dzokha.FastQC.Modules.Shared.BasicStatsDTO;
import vn.dzokha.FastQC.Service.AnalysisResult;
import vn.dzokha.FastQC.Service.FastQCAnalysisService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/fastqc")
@Tag(name = "FastQC Analysis", description = "Các API liên quan đến phân tích chất lượng trình tự DNA")
public class FastQCWebController {

    private static final Logger log = LoggerFactory.getLogger(FastQCWebController.class);

    private final FastQCAnalysisService analysisService;
    private final BasicStatsMapper basicStatsMapper;

    public FastQCWebController(FastQCAnalysisService analysisService, BasicStatsMapper basicStatsMapper) {
        this.analysisService = analysisService;
        this.basicStatsMapper = basicStatsMapper;
    }

    @Operation(summary = "Phân tích file FastQ", description = "Upload danh sách file .fastq hoặc .fastq.gz để nhận kết quả Basic Statistics")
    @PostMapping(value = "/analyze", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompletableFuture<ResponseEntity<?>> analyze(@RequestParam("files") MultipartFile[] files) {
        log.info("Bắt đầu nhận yêu cầu phân tích {} file.", files.length);
        if (files == null || files.length == 0) {
            log.warn("Yêu cầu bị từ chối do không có file.");
            return CompletableFuture.completedFuture(ResponseEntity.badRequest().body("No files."));
        }

        return analysisService.processMultipleFiles(files)
                .<ResponseEntity<?>>thenApply(results -> {
                    log.info("Phân tích thành công cho các file.");
                    // Ép kiểu List rõ ràng
                    List<BasicStatsDTO> dtos = results.stream()
                            .map(result -> basicStatsMapper.toDTO(result.getBasicStats()))
                            .collect(Collectors.toList());
                    
                    // Trả về ResponseEntity rõ ràng
                    return ResponseEntity.ok(dtos);
                })
                .exceptionally(ex -> {
                    log.error("Lỗi nghiêm trọng trong quá trình phân tích: {}", ex.getMessage());
                    // Ép kiểu ResponseEntity trong nhánh lỗi
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Error: " + ex.getMessage());
                });
    }

    @GetMapping("/status")
    public ResponseEntity<String> getStatus() {
        return ResponseEntity.ok("FastQC Analysis Service is active (Java 21/25).");
    }
}