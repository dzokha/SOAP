package vn.dzokha.FastQC.Modules.Sequence;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.itadaki.bzip2.BZip2InputStream;
import vn.dzokha.FastQC.Config.FastQCConfig;
import vn.dzokha.FastQC.Modules.Utilities.MultiMemberGZIPInputStream;

public class FastQFile implements SequenceFile {

    private Sequence nextSequence = null;
    private final String name;
    private final File representativeFile; // File đại diện cho bản Web
    private final InputStream inputStream;
    private final BufferedReader br;
    
    private long lineNumber = 0;
    private boolean isColorspace = false;
    private boolean casavaMode = false;
    private boolean nofilter = false;

    public FastQFile(FastQCConfig config, InputStream inputStream, String fileName) throws SequenceFormatException, IOException {
        this.name = fileName;
        this.representativeFile = new File(fileName); // Tạo object File từ tên
        this.inputStream = inputStream;

        if (config.isCasava()) {
            this.casavaMode = true;
            this.nofilter = config.isNofilter();
        }

        InputStream wrappedStream = inputStream;
        String lowerName = fileName.toLowerCase();

        if (lowerName.endsWith(".gz")) {
            wrappedStream = new MultiMemberGZIPInputStream(inputStream);
        } else if (lowerName.endsWith(".bz2")) {
            wrappedStream = new BZip2InputStream(inputStream, false);
        }

        this.br = new BufferedReader(new InputStreamReader(wrappedStream));
        readNext();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int getPercentComplete() {
        return hasNext() ? 0 : 100;
    }

    @Override
    public boolean isColorspace() {
        return isColorspace;
    }

    @Override
    public boolean hasNext() {
        return nextSequence != null;
    }

    @Override
    public Sequence next() throws SequenceFormatException {
        Sequence seq = nextSequence;
        readNext();
        return seq;
    }

    private void readNext() throws SequenceFormatException {
        try {
            String id;
            while (true) {
                id = br.readLine();
                ++lineNumber;
                if (id == null) {
                    nextSequence = null;
                    close();
                    return;
                }
                if (id.trim().isEmpty()) continue;
                break;
            }

            if (!id.startsWith("@")) {
                throw new SequenceFormatException("ID line didn't start with '@' at line " + lineNumber);
            }

            String seq = br.readLine();
            ++lineNumber;
            String midLine = br.readLine();
            ++lineNumber;
            String quality = br.readLine();
            ++lineNumber;

            if (seq == null || midLine == null || quality == null) {
                throw new IOException("Truncated FastQ file at line " + lineNumber);
            }

            if (!midLine.startsWith("+")) {
                throw new SequenceFormatException("Expected '+' midline at line " + lineNumber);
            }

            if (lineNumber <= 4) {
                checkColorspace(seq);
            }

            // FIX LỖI: Truyền representativeFile thay vì String
            if (isColorspace()) {
                nextSequence = new Sequence(id, convertColorspaceToBases(seq.toUpperCase()), quality, representativeFile);
            } else {
                nextSequence = new Sequence(id, seq.toUpperCase(), quality, representativeFile);
            }

            if (casavaMode && !nofilter && id.contains(":Y:")) {
                nextSequence.setFiltered(true);
            }

        } catch (IOException ioe) {
            throw new SequenceFormatException("Error reading FastQ: " + ioe.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            if (br != null) br.close();
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            // Log quietly
        }
    }

    private void checkColorspace(String seq) {
        String regex = "^[GATCNgatcn][\\.0123456]+$";
        isColorspace = Pattern.compile(regex).matcher(seq).find();
    }

    private String convertColorspaceToBases(String s) {
        // Giữ nguyên logic xử lý chuỗi của bạn tại đây
        return s; 
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }
}