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
package vn.dzokha.FastQC.Modules.Adapter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import vn.dzokha.FastQC.Config.FastQCConfig;
import vn.dzokha.FastQC.Modules.Graphs.BaseGroup;
import vn.dzokha.FastQC.Modules.Graphs.LineGraph;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Sequence.Contaminant.ContaminentFinder;
import vn.dzokha.FastQC.Modules.Shared.AbstractQCModule;
import vn.dzokha.FastQC.Modules.Shared.ModuleConfig;

public class AdapterContent extends AbstractQCModule {

    private int longestSequence = 0;
    private int longestAdapter = 0;
    private long totalCount = 0;
    public boolean calculated = false;

    private Adapter[] adapters;
    private double[][] enrichments = null;
    private String[] labels;
    private String[] xLabels = new String[0];
    private BaseGroup[] groups;

    public AdapterContent() {
        Vector<Adapter> c = new Vector<Adapter>();
        Vector<String> l = new Vector<String>();

        try {
            BufferedReader br = null;
            // Sửa lỗi: Đảm bảo adapter_file trong FastQCConfig là public
            if (FastQCConfig.getInstance().adapter_file == null) {
                InputStream rsrc = ContaminentFinder.class.getResourceAsStream("/Configuration/adapter_list.txt");
                if (rsrc == null) throw new FileNotFoundException("cannot find Configuration/adapter_list.txt");
                br = new BufferedReader(new InputStreamReader(rsrc));
            } else {
                br = new BufferedReader(new FileReader(FastQCConfig.getInstance().adapter_file));
            }

            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#")) continue;
                if (line.trim().length() == 0) continue;

                String[] sections = line.split("\\t+");
                if (sections.length != 2) continue;
                
                Adapter adapter = new Adapter(sections[0], sections[1]);
                c.add(adapter);
                l.add(adapter.name());
                if (adapter.sequence().length() > longestAdapter) longestAdapter = adapter.sequence().length();
            }
            labels = l.toArray(new String[0]);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        adapters = c.toArray(new Adapter[0]);
    }

    // Chuẩn hóa Web: Trả về Object thay vì JPanel
    @Override
    public Object getResultsPanel() {
        if (longestAdapter > longestSequence) return null;
        if (!calculated) calculateEnrichment();
        return new LineGraph(enrichments, 0, 100, "Position in read (bp)", labels, xLabels, "% Adapter");
    }

    @Override
    public void processSequence(Sequence sequence) {
        calculated = false;
        ++totalCount;

        if (sequence.getSequence().length() > longestSequence && sequence.getSequence().length() - longestAdapter > 0) {
            longestSequence = sequence.getSequence().length();
            for (Adapter adapter : adapters) {
                adapter.expandLengthTo((longestSequence - longestAdapter) + 1);
            }
        }

        for (Adapter adapter : adapters) {
            int index = sequence.getSequence().indexOf(adapter.sequence());
            if (index >= 0) {
                for (int i = index; i <= longestSequence - longestAdapter; i++) {
                    adapter.incrementCount(i);
                }
            }
        }
    }

    public synchronized void calculateEnrichment() {
        if (totalCount == 0) return;
        int maxLength = 0;
        for (Adapter adapter : adapters) {
            if (adapter.getPositions().length > maxLength) {
                maxLength = adapter.getPositions().length;
            }
        }

        groups = BaseGroup.makeBaseGroups(maxLength);
        xLabels = new String[groups.length];
        for (int i = 0; i < xLabels.length; i++) {
            xLabels[i] = groups[i].toString();
        }

        enrichments = new double[adapters.length][groups.length];

        for (int a = 0; a < adapters.length; a++) {
            long[] positions = adapters[a].positions;
            for (int g = 0; g < groups.length; g++) {
                for (int p = groups[g].lowerCount() - 1; p < groups[g].upperCount() && p < positions.length; p++) {
                    enrichments[a][g] += (positions[p] * 100d) / totalCount;
                }
                enrichments[a][g] /= (groups[g].upperCount() - groups[g].lowerCount()) + 1;
            }
        }
        calculated = true;
    }

    @Override
    public void reset() {
        calculated = false;
        totalCount = 0;
        longestSequence = 0;
        for (Adapter adapter : adapters) {
            adapter.reset();
        }
    }

    @Override
    public String description() { return "Searches for specific adapter sequences in a library"; }
    
    @Override
    public String name() { return "Adapter Content"; }

    @Override
    public boolean raisesError() {
        if (!calculated) calculateEnrichment();
        if (enrichments == null) return false;
        for (double[] enrichment : enrichments) {
            for (double v : enrichment) {
                if (v > ModuleConfig.getParam("adapter", "error")) return true;
            }
        }
        return false;
    }

    @Override
    public boolean raisesWarning() {
        if (longestAdapter > longestSequence) return true;
        if (!calculated) calculateEnrichment();
        if (enrichments == null) return false;
        for (double[] enrichment : enrichments) {
            for (double v : enrichment) {
                if (v > ModuleConfig.getParam("adapter", "warn")) return true;
            }
        }
        return false;
    }

    @Override
    public boolean ignoreInReport() {
        return false;
    }

    @Override
    public void makeReport(HTMLReportArchive report) throws IOException, XMLStreamException {
        if (longestAdapter > longestSequence) {
            XMLStreamWriter xhtml = report.xhtmlStream();
            xhtml.writeStartElement("p");
            xhtml.writeCharacters("Can't analyse adapters as read length is too short (" + longestAdapter + " vs " + longestSequence + ")");
            xhtml.writeEndElement();
        } else {
            if (!calculated) calculateEnrichment();

            LineGraph graph = new LineGraph(enrichments, 0, 100, "Position in read (bp)", labels, xLabels, "% Adapter");
            writeSpecificImage(report, graph, "adapter_content.png", "Adapter graph", Math.max(800, groups.length * 15), 600);

            StringBuffer sb = report.dataDocument();
            sb.append("#Position");
            for (String label : labels) sb.append("\t").append(label);
            sb.append("\n");

            for (int r = 0; r < enrichments[0].length; r++) {
                sb.append(xLabels[r]);
                for (int c = 0; c < adapters.length; c++) {
                    sb.append("\t").append(enrichments[c][r]);
                }
                sb.append("\n");
            }
        }
    }

    @Override
    public boolean ignoreFilteredSequences() { return true; }

    private class Adapter {
        private String name;
        private String sequence;
        private long[] positions = new long[0];

        public Adapter(String name, String sequence) {
            this.name = name;
            this.sequence = sequence;
            positions = new long[1];
        }

        public void incrementCount(int position) { 
            if (position < positions.length) ++positions[position]; 
        }

        public void expandLengthTo(int newLength) {
            long[] newPositions = new long[newLength];
            System.arraycopy(positions, 0, newPositions, 0, positions.length);
            if (positions.length > 0) {
                for (int i = positions.length; i < newPositions.length; i++) {
                    newPositions[i] = positions[positions.length - 1];
                }
            }
            positions = newPositions;
        }

        public long[] getPositions() { return positions; }
        public String sequence() { return sequence; }
        public void reset() { positions = new long[1]; }
        public String name() { return name; }
    }
}