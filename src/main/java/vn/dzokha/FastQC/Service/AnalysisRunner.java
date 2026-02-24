package vn.dzokha.FastQC.Service;

import org.springframework.stereotype.Component;
import vn.dzokha.FastQC.Modules.Shared.BasicStats;
import vn.dzokha.FastQC.Modules.Shared.QCModule;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Sequence.SequenceFile;
import vn.dzokha.FastQC.Modules.Sequence.SequenceFormatException;

@Component
public class AnalysisRunner {

    /**
     * Chạy phân tích đồng bộ trên luồng được giao bởi Service.
     */
    public void runAnalysisSync(SequenceFile file, QCModule[] modules) throws SequenceFormatException {
        
        // Reset các module trước khi chạy
        for (QCModule module : modules) {
            module.reset();
        }

        int seqCount = 0;
        // Lặp qua từng đoạn sequence trong file
        while (file.hasNext()) {
            seqCount++;
            Sequence seq = file.next();

            for (QCModule module : modules) {
                // Kiểm tra xem module có bỏ qua sequence bị filter không
                if (seq.isFiltered() && module.ignoreFilteredSequences()) {
                    continue;
                }
                module.processSequence(seq);
            }
            
            // Log tiến độ nhẹ nhàng (không cần thread sleep như bản cũ)
            if (seqCount % 10000 == 0) {
                // Có thể thêm logging tại đây nếu cần
            }
        }

        // Xử lý trường hợp file rỗng (giữ nguyên logic gốc)
        if (seqCount == 0) {
            for (QCModule module : modules) {
                if (module instanceof BasicStats) {
                    ((BasicStats) module).setFileName(file.name());
                }
            }
        }
    }
}