package vn.dzokha.FastQC.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.dzokha.FastQC.Modules.Shared.ModuleFactory;
import vn.dzokha.FastQC.Modules.Shared.QCModule;
import vn.dzokha.FastQC.Modules.Sequence.SequenceFactory;
import vn.dzokha.FastQC.Modules.Sequence.SequenceFile;

// Import thiếu khiến Maven báo lỗi
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

import vn.dzokha.FastQC.Config.FastQCProperties; 

@Service
public class FastQCAnalysisService {

    private final FastQCProperties properties;

    public FastQCAnalysisService(FastQCProperties properties) {
        this.properties = properties;
        // Sử dụng tham số
        System.out.println("Upload directory: " + properties.getUploadDir());
    }

    @Autowired
    private SequenceFactory sequenceFactory;

    @Autowired
    private AnalysisRunner analysisRunner;

    /**
     * Xử lý phân tích nhiều file cùng lúc
     */
    public CompletableFuture<List<AnalysisResult>> processMultipleFiles(MultipartFile[] files) {
        List<CompletableFuture<AnalysisResult>> futures = new ArrayList<>();

        for (MultipartFile file : files) {
            // Tận dụng cơ chế @Async đã có để chạy song song từng file
            futures.add(this.processUploadedFile(file));
        }

        // Đợi tất cả các file hoàn thành và gom kết quả lại thành một danh sách
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    @Async("analysisTaskExecutor") 
    public CompletableFuture<AnalysisResult> processUploadedFile(MultipartFile uploadFile) {
        String fileName = uploadFile.getOriginalFilename();
        
        // try-with-resources đảm bảo tự động đóng file
        try (SequenceFile sequenceFile = sequenceFactory.getSequenceFile(fileName, uploadFile.getInputStream())) {
            
            QCModule[] modules = ModuleFactory.getStandardModuleList();
            analysisRunner.runAnalysisSync(sequenceFile, modules);

            return CompletableFuture.completedFuture(
                new AnalysisResult(fileName, modules, "SUCCESS")
            );

        } catch (Exception e) {
            return CompletableFuture.completedFuture(
                new AnalysisResult(fileName, null, "FAILED: " + e.getMessage())
            );
        }
    }
}