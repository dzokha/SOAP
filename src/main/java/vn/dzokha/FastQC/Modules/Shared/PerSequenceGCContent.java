/**
 * Copyright Copyright 2010-17 Simon Andrews
 *
 * This file is part of FastQC.
 *
 * FastQC is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * FastQC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FastQC; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package vn.dzokha.FastQC.Modules.Shared;

import java.io.IOException;
import javax.xml.stream.XMLStreamException;

import vn.dzokha.FastQC.Modules.Graphs.LineGraph;
import vn.dzokha.FastQC.Modules.GCModel.GCModel;
import vn.dzokha.FastQC.Modules.GCModel.GCModelValue;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Statistics.NormalDistribution;

/**
 * Phiên bản hoàn thiện cho Web/Server (Headless mode)
 */
public class PerSequenceGCContent extends AbstractQCModule {

    private double [] gcDistribution = new double[101];
    private double [] theoreticalDistribution = new double[101];
    private int maxCount = 0; 
    private int [] xCategories = new int[0];
    private double max = 0;
    private double deviationPercent;
    private boolean calculated = false;
    private LineGraph graph; 
    
    private GCModel [] cachedModels = new GCModel [200];

    @Override
    public Object getResultsPanel() {
        if (!calculated) calculateDistribution();
        
        double [][] data = new double[2][];
        data[0] = gcDistribution;
        data[1] = theoreticalDistribution;
        
        String [] titles = new String [] {"GC count per read", "Theoretical Distribution"};
        
        // Chuyển đổi xCategories sang String array cho LineGraph
        String [] categories = new String[101];
        for (int i=0; i<101; i++) {
            categories[i] = String.valueOf(i);
        }
        
        // maxCount được dùng làm giới hạn trục Y
        graph = new LineGraph(data, 0, (double)maxCount, "Mean GC content (%)", titles, categories, "GC distribution over all sequences");
        return graph;
    }
    
    public boolean ignoreFilteredSequences() {
        return true;
    }
    
    public boolean ignoreInReport () {
        if (ModuleConfig.getParam("gc_sequence", "ignore") > 0) {
            return true;
        }
        return false;
    }
    
    private synchronized void calculateDistribution () {
        max = 0;
        xCategories = new int[gcDistribution.length];
        double totalCount = 0;
        
        int firstMode = 0;
        double modeCount = 0;
        
        for (int i=0;i<gcDistribution.length;i++) {
            xCategories[i] = i;
            totalCount += gcDistribution[i];
            
            if (gcDistribution[i] > modeCount) {
                modeCount = gcDistribution[i];
                firstMode = i;
            }
            if (gcDistribution[i] > max) max = gcDistribution[i];
        }

        double mode = 0;
        int modeDuplicates = 0;
        boolean fellOffTop = true;

        for (int i=firstMode;i<gcDistribution.length;i++) {
            if (gcDistribution[i] > gcDistribution[firstMode] - (gcDistribution[firstMode]/10)) {
                mode += i;
                modeDuplicates++;
            }
            else {
                fellOffTop = false;
                break;
            }
        }

        boolean fellOffBottom = true;
        for (int i=firstMode-1;i>=0;i--) {
            if (gcDistribution[i] > gcDistribution[firstMode] - (gcDistribution[firstMode]/10)) {
                mode += i;
                modeDuplicates++;
            }
            else {
                fellOffBottom = false;
                break;
            }
        }

        if (fellOffBottom || fellOffTop) {
            mode = firstMode;
        }
        else {
            mode /= modeDuplicates;
        }
        
        double stdev = 0;
        for (int i=0;i<gcDistribution.length;i++) {
            stdev += Math.pow((i-mode),2) * gcDistribution[i];
        }
        
        if (totalCount > 1) {
            stdev /= totalCount-1;
            stdev = Math.sqrt(stdev);
        }
        else {
            stdev = 1;
        }
        
        NormalDistribution nd = new NormalDistribution(mode, stdev);
        deviationPercent = 0;
        
        double tempMax = max;
        for (int i=0; i<theoreticalDistribution.length; i++) {
            double probability = nd.getZScoreForValue(i);
            theoreticalDistribution[i] = probability * totalCount;
            
            if (theoreticalDistribution[i] > tempMax) {
                tempMax = theoreticalDistribution[i];
            }
            deviationPercent += Math.abs(theoreticalDistribution[i]-gcDistribution[i]);
        }
        
        // Cập nhật maxCount để LineGraph vẽ đúng trục Y
        this.maxCount = (int)Math.ceil(tempMax);
        this.max = tempMax;

        if (totalCount > 0) {
            deviationPercent /= totalCount;
            deviationPercent *= 100;
        }
        
        calculated = true;
    }

    public void processSequence(Sequence sequence) {
        char [] seq = truncateSequence(sequence);
        if (seq.length == 0) return; 
        
        int thisSeqGCCount = 0;
        for (int i=0;i<seq.length;i++) {
            if (seq[i] == 'G' || seq[i] == 'C') {
                ++thisSeqGCCount;
            }
        }

        if (seq.length >= cachedModels.length) {
            GCModel [] longerModels = new GCModel[seq.length+1];
            System.arraycopy(cachedModels, 0, longerModels, 0, cachedModels.length);
            cachedModels = longerModels;
        }
        
        if (cachedModels[seq.length] == null) {
            cachedModels[seq.length] = new GCModel(seq.length);
        }

        GCModelValue [] values = cachedModels[seq.length].getModelValues(thisSeqGCCount);
        for (int i=0;i<values.length;i++) {
            gcDistribution[values[i].percentage()] += values[i].increment();
        }
    }
    
    private char [] truncateSequence (Sequence sequence) {
        String seq = sequence.getSequence();
        if (seq.length() > 1000) {
            int length = (seq.length()/1000)*1000;
            return seq.substring(0, length).toCharArray();
        }
        if (seq.length() > 100) {
            int length = (seq.length()/100)*100;
            return seq.substring(0, length).toCharArray();
        }
        return seq.toCharArray();        
    }
    
    public void reset () {
        gcDistribution = new double[101];
        theoreticalDistribution = new double[101];
        maxCount = 0;
        max = 0;
        calculated = false;
    }

    public String description() {
        return "Shows the distribution of GC contents for whole sequences";
    }

    public String name() {
        return "Per sequence GC content";
    }

    public boolean raisesError() {
        if (!calculated) calculateDistribution();
        return deviationPercent > ModuleConfig.getParam("gc_sequence", "error");
    }

    public boolean raisesWarning() {
        if (!calculated) calculateDistribution();
        return deviationPercent > ModuleConfig.getParam("gc_sequence", "warn");
    }

    public void makeReport(HTMLReportArchive report) throws IOException, XMLStreamException {
        if (!calculated) calculateDistribution();
        writeDefaultImage(report, "per_sequence_gc_content.png", "Per sequence GC content graph", 800, 600);
                
        StringBuffer sb = report.dataDocument();
        sb.append("#GC Content\tCount\n");
        for (int i=0;i<gcDistribution.length;i++) {
            sb.append(i);
            sb.append("\t");
            sb.append(gcDistribution[i]);
            sb.append("\n");
        }
    }
}