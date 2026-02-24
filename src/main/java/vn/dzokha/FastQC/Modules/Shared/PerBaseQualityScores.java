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

// XÓA JPanel vì chúng ta đang chạy Headless
// import javax.swing.JPanel; 
import javax.xml.stream.XMLStreamException;

import vn.dzokha.FastQC.Modules.Graphs.BaseGroup;
import vn.dzokha.FastQC.Modules.Graphs.QualityBoxPlot;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Sequence.QualityEncoding.PhredEncoding;
import vn.dzokha.FastQC.Modules.Utilities.QualityCount;

public class PerBaseQualityScores extends AbstractQCModule {

	public QualityCount [] qualityCounts = new QualityCount[0];
	double [] means = null;
	double [] medians = null;
	double [] lowerQuartile = null;
	double [] upperQuartile = null;
	double [] lowest = null;
	double [] highest = null;
	String [] xLabels;
	int low = 0;
	int high = 0;
	PhredEncoding encodingScheme;
	private boolean calculated = false;
	
	// SỬA TẠI ĐÂY: Đổi kiểu trả về từ JPanel thành Object để tương thích với phiên bản Headless
	@Override
	public Object getResultsPanel() {
		
		if (!calculated) getPercentages();

		return new QualityBoxPlot(means, medians, lowest, highest, lowerQuartile, upperQuartile, (double)low, (double)high, 2d, xLabels, "Quality scores across all bases ("+encodingScheme+" encoding)");
	}
	
	public boolean ignoreFilteredSequences() {
		return true;
	}
	
	public boolean ignoreInReport () {
		if (ModuleConfig.getParam("quality_base", "ignore") > 0 || qualityCounts.length == 0) {
			return true;
		}
		return false;
	}

	private synchronized void getPercentages () {
		if (qualityCounts.length == 0) {
			calculated = true;
			return;
		}
		
		char [] range = calculateOffsets();
		encodingScheme = PhredEncoding.getFastQEncodingOffset(range[0]);
		low = 0;
		high = range[1] - encodingScheme.offset();
		if (high < 35) {
			high = 35;
		}
		
		BaseGroup [] groups = BaseGroup.makeBaseGroups(qualityCounts.length);
		
		means = new double[groups.length];
		medians = new double[groups.length];
		lowest = new double[groups.length];
		highest = new double[groups.length];
		lowerQuartile = new double[groups.length];
		upperQuartile = new double[groups.length];
		xLabels = new String[groups.length];
		
		for (int i=0;i<groups.length;i++) {
			xLabels[i] = groups[i].toString();
			int minBase = groups[i].lowerCount();
			int maxBase = groups[i].upperCount();
			lowest[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 10);
			highest[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 90);
			means[i] = getMean(minBase,maxBase,encodingScheme.offset());
			medians[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 50);
			lowerQuartile[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 25);
			upperQuartile[i] = getPercentile(minBase, maxBase, encodingScheme.offset(), 75);
		}

		calculated = true;
	}
	
	private char [] calculateOffsets () {
		char minChar = 0;
		char maxChar = 0;
		
		for (int q=0;q<qualityCounts.length;q++) {
			if (q == 0) {
				minChar = qualityCounts[q].getMinChar();
				maxChar = qualityCounts[q].getMaxChar();
			}
			else {
				if (qualityCounts[q].getMinChar() < minChar && qualityCounts[q].getMinChar() != 0) {
					minChar = qualityCounts[q].getMinChar();
				}
				if (qualityCounts[q].getMaxChar() > maxChar) {
					maxChar = qualityCounts[q].getMaxChar();
				}
			}
		}
		return new char[] {minChar,maxChar};
	}
	
	public void processSequence(Sequence sequence) {
		calculated = false;
		char [] qual = sequence.getQualityString().toCharArray();
		if (qualityCounts.length < qual.length) {
			QualityCount [] qualityCountsNew = new QualityCount[qual.length];
			System.arraycopy(qualityCounts, 0, qualityCountsNew, 0, qualityCounts.length);
			for (int i=qualityCounts.length; i<qualityCountsNew.length; i++) {
				qualityCountsNew[i] = new QualityCount();				
			}
			qualityCounts = qualityCountsNew;
		}
		
		for (int i=0;i<qual.length;i++) {
			qualityCounts[i].addValue(qual[i]);
		}
	}
	
	public void reset () {
		qualityCounts = new QualityCount[0];
		calculated = false;
	}

	public String description() {
		return "Shows the Quality scores of all bases at a given position in a sequencing run";
	}

	public String name() {
		return "Per base sequence quality";
	}

	public boolean raisesError() {
		if (!calculated) getPercentages();
		if (lowerQuartile == null) return false;

		for (int i=0;i<lowerQuartile.length;i++) {
			if (Double.isNaN(lowerQuartile[i])) continue;
			if (lowerQuartile[i] < ModuleConfig.getParam("quality_base_lower", "error") || medians[i] < ModuleConfig.getParam("quality_base_median", "error")) {
				return true;
			}
		}
		return false;
	}

	public boolean raisesWarning() {
		if (!calculated) getPercentages();
		if (lowerQuartile == null) return false;

		for (int i=0;i<lowerQuartile.length;i++) {
			if (Double.isNaN(lowerQuartile[i])) continue;
			if (lowerQuartile[i] < ModuleConfig.getParam("quality_base_lower", "warn") || medians[i] < ModuleConfig.getParam("quality_base_median", "warn")) {
				return true;
			}
		}
		return false;
	}
	
	public void makeReport(HTMLReportArchive report) throws IOException,XMLStreamException {
		if (!calculated) getPercentages();

		writeDefaultImage(report, "per_base_quality.png", "Per base quality graph", Math.max(800, xLabels.length*15), 600);		
		
		StringBuffer sb = report.dataDocument();
		sb.append("#Base\tMean\tMedian\tLower Quartile\tUpper Quartile\t10th Percentile\t90th Percentile\n");
		for (int i=0;i<means.length;i++) {
			sb.append(xLabels[i]).append("\t")
			  .append(means[i]).append("\t")
			  .append(medians[i]).append("\t")
			  .append(lowerQuartile[i]).append("\t")
			  .append(upperQuartile[i]).append("\t")
			  .append(lowest[i]).append("\t")
			  .append(highest[i]).append("\n");
		}
	}
	
	private double getPercentile (int minbp, int maxbp, int offset, int percentile) {
		int count = 0;
		double total = 0;
		for (int i=minbp-1;i<maxbp;i++) {
			if (i < qualityCounts.length && qualityCounts[i].getTotalCount() > 100) {
				count++;
				total += qualityCounts[i].getPercentile(offset, percentile);
			}
		}
		return count > 0 ? total/count : Double.NaN;
	}

	private double getMean (int minbp, int maxbp, int offset) {
		int count = 0;
		double total = 0;
		for (int i=minbp-1;i<maxbp;i++) {
			if (i < qualityCounts.length && qualityCounts[i].getTotalCount() > 0) {
				count++;
				total += qualityCounts[i].getMean(offset);
			}
		}
		return count > 0 ? total/count : 0;
	}
}