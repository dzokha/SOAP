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
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import vn.dzokha.FastQC.Modules.Graphs.LineGraph;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Sequence.QualityEncoding.PhredEncoding;

/**
 * Phân tích phân phối điểm chất lượng trung bình trên mỗi sequence.
 */
public class PerSequenceQualityScores extends AbstractQCModule {

	private HashMap<Integer, Long> averageScoreCounts = new HashMap<Integer, Long>();
	private double [] qualityDistribution = null;
	private int [] xCategories = new int[0];
	private char lowestChar = 126;
	private int maxCount = 0;
	private int mostFrequentScore;
	private boolean calculated = false;
	private LineGraph graph; 
	
	@Override
	public Object getResultsPanel() {
		if (!calculated) calculateDistribution();
		
		if (qualityDistribution == null || qualityDistribution.length == 0) return null;

		double [][] data = new double[1][];
		data[0] = qualityDistribution;
		
		String [] titles = new String [] {"Average Quality per read"};
		
		// Chuyển đổi xCategories (int[]) sang String[] để tương thích với LineGraph mới
		String [] stringCategories = new String[xCategories.length];
		for (int i=0; i<xCategories.length; i++) {
			stringCategories[i] = String.valueOf(xCategories[i]);
		}
		
		// Khởi tạo LineGraph (Headless version)
		graph = new LineGraph(data, 0, (double)maxCount, "Average Sequence Quality (Phred Score)", titles, stringCategories, "Quality score distribution over all sequences");
		return graph;
	}
	
	public boolean ignoreInReport () {
		if (ModuleConfig.getParam("quality_sequence", "ignore") > 0  || averageScoreCounts.size() == 0) {
			return true;
		}
		return false;
	}
	
	private synchronized void calculateDistribution () {
		
		if (averageScoreCounts.size() == 0) {
			qualityDistribution = new double[0];
			xCategories = new int[0];
			calculated = true;
			return;
		}

		PhredEncoding encoding = PhredEncoding.getFastQEncodingOffset(lowestChar);
		
		Integer [] rawScores = averageScoreCounts.keySet().toArray(new Integer [0]);
		Arrays.sort(rawScores);
		
		// Khởi tạo mảng dựa trên dải điểm chất lượng xuất hiện
		qualityDistribution = new double [1+(rawScores[rawScores.length-1]-rawScores[0])] ;
		xCategories = new int[qualityDistribution.length];
		
		maxCount = 0;
		for (int i=0; i<qualityDistribution.length; i++) {
			int currentRawScore = rawScores[0] + i;
			xCategories[i] = currentRawScore - encoding.offset();
			
			if (averageScoreCounts.containsKey(currentRawScore)) {
				qualityDistribution[i] = averageScoreCounts.get(currentRawScore);
			}
			
			if (qualityDistribution[i] > maxCount) {
				maxCount = (int)qualityDistribution[i];
				mostFrequentScore = xCategories[i];
			}
		}
				
		calculated = true;
	}

	public void processSequence(Sequence sequence) {
		String qualityString = sequence.getQualityString();
		if (qualityString == null || qualityString.length() == 0) return;

		char [] seq = qualityString.toCharArray();
		long totalQuality = 0;
		
		for (int i=0; i<seq.length; i++) {
			if (seq[i] < lowestChar) {
				lowestChar = seq[i];
			}
			totalQuality += seq[i];
		}

		int averageQuality = (int)(totalQuality / seq.length);
				
		if (averageScoreCounts.containsKey(averageQuality)) {
			averageScoreCounts.put(averageQuality, averageScoreCounts.get(averageQuality) + 1);
		}
		else {
			averageScoreCounts.put(averageQuality, 1L);
		}
	}
	
	public void reset () {
		averageScoreCounts.clear();
		lowestChar = 126;
		maxCount = 0;
		calculated = false;
		qualityDistribution = null;
		xCategories = new int[0];
	}

	public String description() {
		return "Shows the distribution of average quality scores for whole sequences";
	}

	public String name() {
		return "Per sequence quality scores";
	}

	public boolean raisesError() {
		if (!calculated) calculateDistribution();
		if (xCategories.length == 0) return false;
		return mostFrequentScore <= ModuleConfig.getParam("quality_sequence", "error");
	}

	public boolean raisesWarning() {
		if (!calculated) calculateDistribution();
		if (xCategories.length == 0) return false;
		return mostFrequentScore <= ModuleConfig.getParam("quality_sequence", "warn");
	}

	public void makeReport(HTMLReportArchive report) throws IOException, XMLStreamException {
		if (!calculated) calculateDistribution();
		
		writeDefaultImage(report, "per_sequence_quality.png", "Per Sequence quality graph", 800, 600);

		StringBuffer sb = report.dataDocument();
		sb.append("#Quality\tCount\n");
		for (int i=0; i<xCategories.length; i++) {
			sb.append(xCategories[i]);
			sb.append("\t");
			sb.append(qualityDistribution[i]);
			sb.append("\n");
		}
	}

	public boolean ignoreFilteredSequences() {
		return true;
	}
}