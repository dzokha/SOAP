/**
 * Copyright Copyright 2010-17 Simon Andrews
 *
 * This file is part of FastQC.
 *
 * FastQC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package vn.dzokha.FastQC.Modules.Report;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import vn.dzokha.FastQC.Config.FastQCConfig;
import vn.dzokha.FastQC.Modules.Shared.QCModule;
import vn.dzokha.FastQC.Modules.Sequence.SequenceFile;
import vn.dzokha.FastQC.Modules.Utilities.ImageToBase64;

public class HTMLReportArchive {
    // CÁC BIẾN NÀY RẤT QUAN TRỌNG - KHÔNG ĐƯỢC XÓA
    private XMLStreamWriter xhtml = null;
    private StringBuffer data = new StringBuffer();
    private QCModule[] modules;
    private ZipOutputStream zip;
    private SequenceFile sequenceFile;
    private byte[] buffer = new byte[1024];
    private File htmlFile;
    private File zipFile;

    public HTMLReportArchive(SequenceFile sequenceFile, QCModule[] modules, File htmlFile) throws IOException, XMLStreamException {
        this.sequenceFile = sequenceFile;
        this.modules = modules;
        this.htmlFile = htmlFile;
        this.zipFile = new File(htmlFile.getAbsolutePath().replaceAll("\\.html$", "") + ".zip");
        
        StringWriter htmlStr = new StringWriter();
        XMLOutputFactory xmlfactory = XMLOutputFactory.newInstance();
        this.xhtml = xmlfactory.createXMLStreamWriter(htmlStr);

        zip = new ZipOutputStream(new FileOutputStream(zipFile));
        zip.putNextEntry(new ZipEntry(folderName() + "/"));
        zip.putNextEntry(new ZipEntry(folderName() + "/Icons/"));
        zip.putNextEntry(new ZipEntry(folderName() + "/Images/"));
        
        startDocument();
        for (int m = 0; m < modules.length; m++) {
            if (modules[m].ignoreInReport()) continue;
            xhtml.writeStartElement("div");
            xhtml.writeAttribute("class", "module");
            xhtml.writeStartElement("h2");
            xhtml.writeAttribute("id", "M" + m);

            if (modules[m].raisesError()) {
                xhtml.writeEmptyElement("img");
                xhtml.writeAttribute("src", base64ForIcon("Icons/error.png"));
                xhtml.writeAttribute("alt", "[FAIL]");
            } else if (modules[m].raisesWarning()) {
                xhtml.writeEmptyElement("img");
                xhtml.writeAttribute("src", base64ForIcon("Icons/warning.png"));
                xhtml.writeAttribute("alt", "[WARN]");
            } else {
                xhtml.writeEmptyElement("img");
                xhtml.writeAttribute("src", base64ForIcon("Icons/tick.png"));
                xhtml.writeAttribute("alt", "[OK]");
            }

            xhtml.writeCharacters(modules[m].name());
            data.append(">>").append(modules[m].name()).append("\t");
            if (modules[m].raisesError()) data.append("fail");
            else if (modules[m].raisesWarning()) data.append("warn");
            else data.append("pass");
            data.append("\n");
            
            xhtml.writeEndElement(); // h2
            modules[m].makeReport(this);
            data.append(">>END_MODULE\n");
            xhtml.writeEndElement(); // div module
        }
        closeDocument();

        zip.putNextEntry(new ZipEntry(folderName() + "/fastqc_report.html"));
        xhtml.flush();
        zip.write(htmlStr.toString().getBytes());
        zip.closeEntry();
        
        zip.putNextEntry(new ZipEntry(folderName() + "/fastqc_data.txt"));
        zip.write(data.toString().getBytes());
        zip.closeEntry();

        // XSL-FO (Giữ nguyên logic cũ)
        try {
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document src = builder.parse(new InputSource(new StringReader(htmlStr.toString())));
            InputStream rsrc = getClass().getResourceAsStream("/Templates/fastqc2fo.xsl");
            if (rsrc != null) {
                domFactory.setNamespaceAware(true);
                builder = domFactory.newDocumentBuilder();
                Document html2fo = builder.parse(rsrc);
                TransformerFactory tf = TransformerFactory.newInstance();
                Templates templates = tf.newTemplates(new DOMSource(html2fo));
                zip.putNextEntry(new ZipEntry(folderName() + "/fastqc.fo"));
                templates.newTransformer().transform(new DOMSource(src), new StreamResult(zip));
                zip.closeEntry();
                rsrc.close();
            }
        } catch (Exception e) { e.printStackTrace(); }

        zip.close();

        // Ghi file HTML
        try (PrintWriter pr = new PrintWriter(new FileWriter(htmlFile))) {
            pr.print(htmlStr.toString());
        }

        if (FastQCConfig.getInstance().do_unzip) {
            unzipZipFile(zipFile);
        }
    }

    private void startDocument() throws IOException, XMLStreamException {
        data.append("##FastQC\t0.12.1\n");
        for (String icnName : new String[]{"fastqc_icon.png", "warning.png", "error.png", "tick.png"}) {
            InputStream in = getClass().getResourceAsStream("/Templates/Icons/" + icnName);
            if (in == null) continue;
            zip.putNextEntry(new ZipEntry(folderName() + "/Icons/" + icnName));
            int len;
            while ((len = in.read(buffer)) > 0) { zip.write(buffer, 0, len); }
            in.close();
            zip.closeEntry();
        }

        SimpleDateFormat df = new SimpleDateFormat("EEE d MMM yyyy");
        xhtml.writeDTD("<!DOCTYPE html>");
        xhtml.writeStartElement("html");
        xhtml.writeStartElement("head");
        xhtml.writeStartElement("title");
        xhtml.writeCharacters(sequenceFile.name() + " FastQC Report");
        xhtml.writeEndElement();

        InputStream rsrc = getClass().getResourceAsStream("/Templates/header_template.html");
        if (rsrc != null) {
            xhtml.writeStartElement("style");
            xhtml.writeAttribute("type", "text/css");
            byte[] arr = new byte[1024];
            int n;
            while ((n = rsrc.read(arr)) != -1) { xhtml.writeCharacters(new String(arr, 0, n)); }
            rsrc.close();
            xhtml.writeEndElement();
        }
        xhtml.writeEndElement(); // head

        xhtml.writeStartElement("body");
        xhtml.writeStartElement("div");
        xhtml.writeAttribute("class", "header");
        xhtml.writeStartElement("div");
        xhtml.writeAttribute("id", "header_title");
        xhtml.writeEmptyElement("img");
        xhtml.writeAttribute("src", base64ForIcon("Icons/fastqc_icon.png"));
        xhtml.writeAttribute("alt", "FastQC");
        xhtml.writeCharacters("FastQC Report");
        xhtml.writeEndElement();

        xhtml.writeStartElement("div");
        xhtml.writeAttribute("id", "header_filename");
        xhtml.writeCharacters(df.format(new Date()));
        xhtml.writeEmptyElement("br");
        xhtml.writeCharacters(sequenceFile.name());
        xhtml.writeEndElement();
        xhtml.writeEndElement(); // header

        xhtml.writeStartElement("div");
        xhtml.writeAttribute("class", "summary");
        xhtml.writeStartElement("h2");
        xhtml.writeCharacters("Summary");
        xhtml.writeEndElement();
        xhtml.writeStartElement("ul");

        StringBuilder summaryText = new StringBuilder();
        for (int m = 0; m < modules.length; m++) {
            if (modules[m].ignoreInReport()) continue;
            xhtml.writeStartElement("li");
            xhtml.writeEmptyElement("img");
            if (modules[m].raisesError()) {
                xhtml.writeAttribute("src", base64ForIcon("Icons/error.png"));
                summaryText.append("FAIL");
            } else if (modules[m].raisesWarning()) {
                xhtml.writeAttribute("src", base64ForIcon("Icons/warning.png"));
                summaryText.append("WARN");
            } else {
                xhtml.writeAttribute("src", base64ForIcon("Icons/tick.png"));
                summaryText.append("PASS");
            }
            summaryText.append("\t").append(modules[m].name()).append("\t").append(sequenceFile.name()).append("\n");
            
            xhtml.writeStartElement("a");
            xhtml.writeAttribute("href", "#M" + m);
            xhtml.writeCharacters(modules[m].name());
            xhtml.writeEndElement();
            xhtml.writeEndElement();
        }
        xhtml.writeEndElement(); // ul
        xhtml.writeEndElement(); // summary

        xhtml.writeStartElement("div");
        xhtml.writeAttribute("class", "main");
        
        zip.putNextEntry(new ZipEntry(folderName() + "/summary.txt"));
        zip.write(summaryText.toString().getBytes());
        zip.closeEntry();
    }

    private String base64ForIcon(String path) {
        try (InputStream is = getClass().getResourceAsStream("/Templates/" + path)) {
            if (is == null) return "";
            BufferedImage b = ImageIO.read(is);
            return ImageToBase64.imageToBase64(b);
        } catch (Exception e) { return ""; }
    }

    private void closeDocument() throws XMLStreamException {
        xhtml.writeEndElement(); // main
        xhtml.writeStartElement("div");
        xhtml.writeAttribute("class", "footer");
        xhtml.writeCharacters("Produced by ");
        xhtml.writeStartElement("a");
        xhtml.writeAttribute("href", "http://www.bioinformatics.babraham.ac.uk/projects/fastqc/");
        xhtml.writeCharacters("FastQC");
        xhtml.writeEndElement();
        xhtml.writeCharacters("  (version 0.12.1)");
        xhtml.writeEndElement();
        xhtml.writeEndElement(); // body
        xhtml.writeEndElement(); // html
    }

    private void unzipZipFile(File file) throws IOException {
        try (ZipFile zf = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File dest = new File(file.getParent(), entry.getName());
                if (entry.isDirectory()) {
                    dest.mkdirs();
                    continue;
                }
                dest.getParentFile().mkdirs();
                try (InputStream is = zf.getInputStream(entry);
                     OutputStream os = new FileOutputStream(dest)) {
                    byte[] b = new byte[1024];
                    int n;
                    while ((n = is.read(b)) != -1) os.write(b, 0, n);
                }
            }
        }
    }

    public String folderName() { return htmlFile.getName().replaceAll("\\.html$", ""); }
    public XMLStreamWriter xhtmlStream() { return this.xhtml; }
    public StringBuffer dataDocument() { return data; }
    public ZipOutputStream zipFile() {
	    return this.zip;
	}
}