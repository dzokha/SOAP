package vn.dzokha.FastQC.Modules.Shared;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;

/**
 * Interface QCModule đã được chuẩn hóa để hỗ trợ cả Web và Headless processing.
 */
public interface QCModule {

    public void processSequence(Sequence sequence);

    /**
     * Thay đổi từ JPanel sang Object để linh hoạt:
     * - Bản Web: Trả về DTO hoặc mảng dữ liệu để convert sang JSON.
     * - Bản Desktop: Có thể vẫn trả về JPanel nếu cần.
     */
    public Object getResultsPanel();
    
    public String name ();
    
    public String description ();
    
    public void reset ();
    
    public boolean raisesError();
    
    public boolean raisesWarning();
    
    public boolean ignoreFilteredSequences();
    
    public boolean ignoreInReport();

    public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException;
}