package vn.dzokha.FastQC.Modules.Kmer;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.math3.distribution.BinomialDistribution;
import vn.dzokha.FastQC.Config.FastQCConfig;
import vn.dzokha.FastQC.Modules.Graphs.BaseGroup;
import vn.dzokha.FastQC.Modules.Graphs.LineGraph;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Shared.AbstractQCModule;
import vn.dzokha.FastQC.Modules.Shared.ModuleConfig;

public class KmerContent extends AbstractQCModule {
    private Hashtable<String, Kmer> kmers = new Hashtable<String, Kmer>();
    private int longestSequence = 0;
    private long [][] totalKmerCounts = new long [500][32]; // Khởi tạo kích thước an toàn
    private long skipCount = 0;
    private static int MIN_KMER_SIZE = 7;
    private static int MAX_KMER_SIZE = 7;
    public boolean calculated = false;
    private Kmer [] enrichedKmers = null;
    private double [][] enrichments = null;
    private double minGraphValue = 0;
    private double maxGraphValue = 0;
    private String [] xCategories = new String[0];
    private String [] xLabels = new String[0];
    private BaseGroup [] groups;

    public KmerContent () {
        if (FastQCConfig.getInstance().kmer_size != 0) {
            int kmerSize = FastQCConfig.getInstance().kmer_size;
            MIN_KMER_SIZE = kmerSize;
            MAX_KMER_SIZE = kmerSize;
        }
    }

    @Override
    public Object getResultsPanel() { return null; }

    @Override
    public boolean ignoreFilteredSequences() { return true; }

    @Override
    public boolean ignoreInReport () { return ModuleConfig.getParam("kmer", "ignore") > 0; }

    @Override
    public void processSequence(Sequence sequence) {
        calculated = false;
        ++skipCount;
        if (skipCount % 50 != 0) return;

        String seq = sequence.getSequence();
        if (seq.length() > 500) seq = seq.substring(0, 500);
        if (seq.length() > longestSequence) longestSequence = seq.length();
                        
        for (int kmerSize=MIN_KMER_SIZE; kmerSize<=MAX_KMER_SIZE; kmerSize++) {
            for (int i=0; i<=seq.length()-kmerSize; i++) {
                String kmer = seq.substring(i, i+kmerSize);
                
                // Cập nhật tổng đếm để tính toán xác suất sau này
                totalKmerCounts[i][kmerSize-1]++;

                if (kmer.indexOf("N") >= 0) continue;

                if (kmers.containsKey(kmer)) {
                    kmers.get(kmer).incrementCount(i);
                } else {
                    kmers.put(kmer, new Kmer(kmer, i, 500));
                }
            }
        }
    }

    private synchronized void calculateEnrichment () {
        if (longestSequence == 0) return;
        groups = BaseGroup.makeBaseGroups((longestSequence-MIN_KMER_SIZE)+1);
        Vector<Kmer> unevenKmers = new Vector<Kmer>();
        Iterator<Kmer> rawKmers = kmers.values().iterator();
        
        while (rawKmers.hasNext()) {
            Kmer k = rawKmers.next();
            long totalKmerOfThisLength = 0;

            for (int i=0; i<totalKmerCounts.length; i++) {
                totalKmerOfThisLength += totalKmerCounts[i][k.sequence().length()-1];
            }

            if (totalKmerOfThisLength == 0) continue;
            float expectedProportion = k.count / (float)totalKmerOfThisLength;
            
            float [] obsExpPositions = new float[groups.length];
            float [] binomialPValues = new float[groups.length];
            long [] positionCounts = k.getPositions();
                
            for (int g=0; g<groups.length; g++) {
                long totalGroupCount = 0;
                long totalGroupHits = 0;
                for (int p=groups[g].lowerCount()-1; p<groups[g].upperCount() && p < positionCounts.length; p++) {
                    totalGroupCount += totalKmerCounts[p][k.sequence().length()-1];
                    totalGroupHits += positionCounts[p];
                }
                            
                if (totalGroupCount == 0 || expectedProportion == 0) continue;
                float predicted = expectedProportion * totalGroupCount;
                obsExpPositions[g] = (float)(totalGroupHits/predicted);
                
                BinomialDistribution bd = new BinomialDistribution((int)totalGroupCount, expectedProportion);
                if (totalGroupHits > predicted) {
                    binomialPValues[g] = (float)((1 - bd.cumulativeProbability((int)totalGroupHits)) * Math.pow(4, k.sequence().length()));
                } else {
                    binomialPValues[g] = 1;
                }
            }
            
            k.setObsExpPositions(obsExpPositions);
            float lowestP = 1;
            for (float p : binomialPValues) if (p < lowestP) lowestP = p;
            
            if (lowestP < 0.01) {
                k.setLowestPValue(lowestP);
                unevenKmers.add(k);
            }
        }
        
        enrichedKmers = unevenKmers.toArray(new Kmer[0]);
        Arrays.sort(enrichedKmers);
        if (enrichedKmers.length > 20) enrichedKmers = Arrays.copyOf(enrichedKmers, 20);
        
        enrichments = new double [Math.min(6, enrichedKmers.length)][groups.length];
        xLabels = new String[enrichments.length];
        xCategories = new String [groups.length];
        for (int i=0; i<groups.length; i++) xCategories[i] = groups[i].toString();
        
        for (int k=0; k<enrichments.length; k++) {
            float [] obsExpPos = enrichedKmers[k].getObsExpPositions();
            for (int g=0; g<groups.length; g++) {                
                enrichments[k][g] = obsExpPos[g];
                if (obsExpPos[g] > maxGraphValue) maxGraphValue = obsExpPos[g];
            }
            xLabels[k] = enrichedKmers[k].sequence();
        }
        kmers.clear();
        calculated = true;
    }

    @Override
    public void makeReport(HTMLReportArchive report) throws IOException, XMLStreamException {
        if (!calculated) calculateEnrichment();
        if (enrichedKmers != null && enrichedKmers.length > 0) {
            writeSpecificImage(report, new LineGraph(enrichments, 0, maxGraphValue, "Position in read (bp)", xLabels, xCategories, "Obs/Exp"), "kmer_profiles.png", "Kmer graph", 800, 600);
        }
    }

    @Override public String name() { return "Kmer Content"; }
    @Override public String description() { return "Identifies short sequences with uneven representation"; }
    @Override public boolean raisesError() { return false; }
    @Override public boolean raisesWarning() { return false; }
    @Override public void reset() { kmers.clear(); calculated=false; }

    private class Kmer implements Comparable<Kmer> {
        private String sequence;
        private float lowestPValue = 1;
        private float [] obsExpPositions;
        private long [] positions;
        private long count = 0;

        public Kmer(String s, int p, int maxLen) { 
            this.sequence = s; 
            this.positions = new long[maxLen];
            incrementCount(p);
        }
        
        public void incrementCount(int p) {
            if (p < positions.length) {
                positions[p]++;
                count++;
            }
        }

        public long[] getPositions() { return positions; }
        public String sequence() { return sequence; }
        public float[] getObsExpPositions() { return obsExpPositions; }
        public void setObsExpPositions(float[] f) { this.obsExpPositions = f; }
        public void setLowestPValue(float f) { this.lowestPValue = f; }
        
        @Override 
        public int compareTo(Kmer o) { 
            return Float.compare(this.lowestPValue, o.lowestPValue); 
        }
    }
}