package vn.dzokha.FastQC.Modules.Shared;

import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.swing.table.TableModel;

import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Utilities.ImageToBase64;
import vn.dzokha.FastQC.Modules.Graphs.LineGraph;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;

public abstract class AbstractQCModule implements QCModule {

    protected boolean calculated = false;

    // HÀM QUAN TRỌNG: Thỏa mãn interface QCModule (Bản Web trả về Object)
    @Override
    public Object getResultsPanel() {
        return null;
    }

    @Override
    public boolean ignoreInReport() {
        return false;
    }

    @Override
    public boolean ignoreFilteredSequences() {
        return true;
    }

    protected void writeSpecificImage(HTMLReportArchive report, Object graphObject, String fileName, String imageTitle, int width, int height) throws IOException, XMLStreamException {
        if (graphObject == null) return;

        ZipOutputStream zip = report.zipFile();
        
        // Tạo ảnh trong bộ nhớ (Headless mode)
        BufferedImage b = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = b.createGraphics();
        
        try {
            // Kiểm tra đối tượng truyền vào để vẽ
            if (graphObject instanceof LineGraph) {
                ((LineGraph) graphObject).render(g, width, height);
            } else if (graphObject instanceof javax.swing.JPanel) {
                javax.swing.JPanel panel = (javax.swing.JPanel) graphObject;
                panel.setSize(width, height);
                panel.print(g);
            }
        } finally {
            g.dispose();
        }

        // Ghi vào file ZIP
        zip.putNextEntry(new ZipEntry(report.folderName() + "/Images/" + fileName));
        ImageIO.write(b, "PNG", zip);
        zip.closeEntry();

        // Chèn vào HTML dưới dạng Base64
        XMLStreamWriter xhtml = report.xhtmlStream();
        xhtml.writeStartElement("p");
        xhtml.writeEmptyElement("img");
        xhtml.writeAttribute("src", "data:image/png;base64," + ImageToBase64.imageToBase64(b));
        xhtml.writeAttribute("alt", imageTitle);
        xhtml.writeEndElement();
    }

    protected void writeTable(HTMLReportArchive report, TableModel table) throws IOException, XMLStreamException {
        XMLStreamWriter w = report.xhtmlStream();
        w.writeStartElement("table");
        
        // Vẽ Header
        w.writeStartElement("tr");
        for (int i = 0; i < table.getColumnCount(); i++) {
            w.writeStartElement("th");
            w.writeCharacters(table.getColumnName(i));
            w.writeEndElement();
        }
        w.writeEndElement();

        // Vẽ Data
        for (int i = 0; i < table.getRowCount(); i++) {
            w.writeStartElement("tr");
            for (int j = 0; j < table.getColumnCount(); j++) {
                w.writeStartElement("td");
                Object val = table.getValueAt(i, j);
                w.writeCharacters(val == null ? "" : val.toString());
                w.writeEndElement();
            }
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    protected void writeDefaultImage(HTMLReportArchive report, String fileName, String imageTitle, int width, int height) throws IOException, XMLStreamException {
        writeSpecificImage(report, getResultsPanel(), fileName, imageTitle, width, height);
    }

    // Các hàm abstract bắt buộc các class con phải thực hiện
    public abstract void processSequence(Sequence sequence);
    public abstract void reset();
    public abstract String name();
    public abstract String description();
    public abstract boolean raisesError();
    public abstract boolean raisesWarning();
    public abstract void makeReport(HTMLReportArchive report) throws XMLStreamException, IOException;
}