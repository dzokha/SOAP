package vn.dzokha.FastQC.Modules.Shared;

import vn.dzokha.FastQC.Modules.Shared.BasicStatsDTO;
import vn.dzokha.FastQC.Modules.Sequence.QualityEncoding.PhredEncoding;
import java.util.ArrayList;
import java.util.List;

import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class BasicStats extends AbstractQCModule {
    private String name = null;
    private long actualCount = 0;
    private long filteredCount = 0;
    private int minLength = 0;
    private int maxLength = 0;
    private long totalBases = 0;
    private long gCount = 0;
    private long cCount = 0;
    private long aCount = 0;
    private long tCount = 0;
    private long nCount = 0;
    private char lowestChar = 126;
    private String fileType = null;

    public String description() {
        return "Calculates some basic statistics about the file";
    }
    
    public boolean ignoreFilteredSequences() {
        return false;
    }

    public void reset() {
        actualCount = 0;
        filteredCount = 0;
        totalBases = 0;
        minLength = 0;
        maxLength = 0;
        gCount = 0;
        cCount = 0;
        aCount = 0;
        tCount = 0;
        nCount = 0;
        lowestChar = 126;
        fileType = null;
    }

    public String name() {
        return "Basic Statistics";
    }
    
    public void setFileName(String name) {
        if (name == null) return;
        this.name = name.replaceFirst("stdin:", "");
    }

    /**
     * Trả về kết quả phân tích dưới dạng List DTO để API Web convert sang JSON
     */
    public List<BasicStatsDTO> getResultsForWeb() {
        List<BasicStatsDTO> stats = new ArrayList<>();
        
        stats.add(new BasicStatsDTO("Filename", name));
        stats.add(new BasicStatsDTO("File type", fileType));
        // SỬA LỖI: Ép kiểu toString() cho PhredEncoding
        stats.add(new BasicStatsDTO("Encoding", PhredEncoding.getFastQEncodingOffset(lowestChar).toString()));
        stats.add(new BasicStatsDTO("Total Sequences", String.valueOf(actualCount)));
        stats.add(new BasicStatsDTO("Total Bases", formatLength(totalBases)));
        stats.add(new BasicStatsDTO("Sequences flagged as poor quality", String.valueOf(filteredCount)));
        
        String lengthVal = (minLength == maxLength) ? String.valueOf(minLength) : minLength + "-" + maxLength;
        stats.add(new BasicStatsDTO("Sequence length", lengthVal));

        long totalATGC = aCount + tCount + gCount + cCount;
        String gcContent = (totalATGC > 0) ? String.valueOf(((gCount + cCount) * 100) / totalATGC) : "0";
        stats.add(new BasicStatsDTO("%GC", gcContent));

        return stats;
    }
    
    @Override
    public Object getResultsPanel() {
        return getResultsForWeb();
    }

    public void processSequence(Sequence sequence) {
        // SỬA LỖI: Sử dụng getName() và kiểm tra null cho file
        if (name == null && sequence.file() != null) {
            setFileName(sequence.file().getName());
        }
        
        if (sequence.isFiltered()) {
            filteredCount++;
            return;
        }
        
        actualCount++;
        int currentLength = sequence.getSequence().length();
        totalBases += currentLength;
        
        if (fileType == null) {
            // SỬA LỖI: Kiểm tra boolean thay vì so sánh null
            if (sequence.getColorspace()) {
                fileType = "Colorspace converted to bases";
            } else {
                fileType = "Conventional base calls";
            }
        }
        
        if (actualCount == 1) {
            minLength = currentLength;
            maxLength = currentLength;
        } else {
            if (currentLength < minLength) minLength = currentLength;
            if (currentLength > maxLength) maxLength = currentLength;
        }

        char[] chars = sequence.getSequence().toCharArray();
        for (char aChar : chars) {
            switch (aChar) {
                case 'G': ++gCount; break;
                case 'A': ++aCount; break;
                case 'T': ++tCount; break;
                case 'C': ++cCount; break;
                case 'N': ++nCount; break;
            }
        }
        
        char[] quals = sequence.getQualityString().toCharArray();
        for (char q : quals) {
            if (q < lowestChar) {
                lowestChar = q;
            }
        }
    }
    
    public boolean raisesError() { return false; }
    public boolean raisesWarning() { return false; }
    public boolean ignoreInReport() { return false; }
    
    public static String formatLength(long originalLength) {
        double length = originalLength;
        String unit = " bp";

        if (length >= 1000000000) {
            length /= 1000000000;
            unit = " Gbp";
        } else if (length >= 1000000) {
            length /= 1000000;
            unit = " Mbp";
        } else if (length >= 1000) {
            length /= 1000;
            unit = " kbp";
        }

        String rawLength = String.format("%.1f", length);
        if (rawLength.endsWith(".0")) {
            rawLength = rawLength.substring(0, rawLength.length() - 2);
        }
        
        return rawLength + unit;
    }

    @Override
    public void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException {
        // Bản Web không dùng ResultsTable (Swing), ta có thể xuất bảng thủ công hoặc để trống nếu report được xử lý ở Frontend
    }
}