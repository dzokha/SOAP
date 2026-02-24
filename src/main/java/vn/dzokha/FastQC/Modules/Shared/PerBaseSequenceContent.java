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

import vn.dzokha.FastQC.Modules.Graphs.BaseGroup;
import vn.dzokha.FastQC.Modules.Graphs.LineGraph;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;

public class PerBaseSequenceContent extends AbstractQCModule {

	public long [] gCounts = new long [0];
	public long [] aCounts = new long [0];
	public long [] cCounts = new long [0];
	public long [] tCounts = new long [0];
	private double [][] percentages = null;
	private String [] xCategories = new String[0];
	private boolean calculated = false;
	private LineGraph graph; // Khai báo biến graph để khắc phục lỗi biên dịch

	@Override
	public Object getResultsPanel() { 
		if (!calculated) getPercentages();
		
		String [] titles = new String [] {"%G", "%A", "%T", "%C"};
		
		// percentages[0]=T, 1=C, 2=A, 3=G. 
		// Sắp xếp lại mảng data theo đúng thứ tự của titles (G-A-T-C)
		double [][] data = new double [][] {
			percentages[3], // %G
			percentages[2], // %A
			percentages[0], // %T
			percentages[1]  // %C
		};
		
		graph = new LineGraph(data, 0, 100, "Position in read (bp)", titles, xCategories, "Per base sequence content");
		return graph;
	}
	
	public boolean ignoreFilteredSequences() {
		return true;
	}
	
	public boolean ignoreInReport () {
		if (ModuleConfig.getParam("sequence", "ignore") > 0) {
			return true;
		}
		return false;
	}

	private synchronized void getPercentages () {
		if (gCounts.length == 0) return;

		BaseGroup [] groups = BaseGroup.makeBaseGroups(gCounts.length);
		xCategories = new String[groups.length];

		double [] gPercent = new double[groups.length];
		double [] aPercent = new double[groups.length];
		double [] tPercent = new double[groups.length];
		double [] cPercent = new double[groups.length];

		for (int i=0; i<groups.length; i++) {
			xCategories[i] = groups[i].toString();

			long gCount = 0;
			long aCount = 0;
			long tCount = 0;
			long cCount = 0;
			long total = 0;
			
			for (int bp=groups[i].lowerCount()-1; bp<groups[i].upperCount(); bp++) {
				if (bp >= gCounts.length) break;

				total += gCounts[bp] + cCounts[bp] + aCounts[bp] + tCounts[bp];
				aCount += aCounts[bp];
				tCount += tCounts[bp];
				cCount += cCounts[bp];
				gCount += gCounts[bp];				
			}
			
			if (total > 0) {
				gPercent[i] = (gCount/(double)total)*100;
				aPercent[i] = (aCount/(double)total)*100;
				tPercent[i] = (tCount/(double)total)*100;
				cPercent[i] = (cCount/(double)total)*100;
			} else {
				gPercent[i] = aPercent[i] = tPercent[i] = cPercent[i] = 0;
			}
		}
		
		percentages = new double [][] {tPercent, cPercent, aPercent, gPercent};
		calculated = true;
	}
	
	public void processSequence(Sequence sequence) {
		calculated = false;
		char [] seq = sequence.getSequence().toCharArray();
		
		if (gCounts.length < seq.length) {
			int newLen = seq.length;
			long [] gCountsNew = new long [newLen];
			long [] aCountsNew = new long [newLen];
			long [] cCountsNew = new long [newLen];
			long [] tCountsNew = new long [newLen];

			System.arraycopy(gCounts, 0, gCountsNew, 0, gCounts.length);
			System.arraycopy(aCounts, 0, aCountsNew, 0, aCounts.length);
			System.arraycopy(tCounts, 0, tCountsNew, 0, tCounts.length);
			System.arraycopy(cCounts, 0, cCountsNew, 0, cCounts.length);

			gCounts = gCountsNew;
			aCounts = aCountsNew;
			tCounts = tCountsNew;
			cCounts = cCountsNew;
		}
		
		for (int i=0; i<seq.length; i++) {
			switch (seq[i]) {
				case 'G': ++gCounts[i]; break;
				case 'A': ++aCounts[i]; break;
				case 'T': ++tCounts[i]; break;
				case 'C': ++cCounts[i]; break;
			}
		}
	}
	
	public void reset () {
		gCounts = new long[0];
		aCounts = new long[0];
		tCounts = new long[0];
		cCounts = new long[0];
		calculated = false;
	}

	public String description() {
		return "Shows the relative amounts of each base at each position in a sequencing run";
	}

	public String name() {
		return "Per base sequence content";
	}

	public boolean raisesError() {
		if (!calculated) getPercentages();
		if (percentages == null) return false;

		for (int i=0; i<percentages[0].length; i++) {
			double gcDiff = Math.abs(percentages[1][i]-percentages[3][i]);
			double atDiff = Math.abs(percentages[0][i]-percentages[2][i]);
			if (gcDiff > ModuleConfig.getParam("sequence", "error") || atDiff > ModuleConfig.getParam("sequence", "error")) return true;
		}
		return false;
	}

	public boolean raisesWarning() {
		if (!calculated) getPercentages();
		if (percentages == null) return false;

		for (int i=0; i<percentages[0].length; i++) {
			double gcDiff = Math.abs(percentages[1][i]-percentages[3][i]);
			double atDiff = Math.abs(percentages[0][i]-percentages[2][i]);
			if (gcDiff > ModuleConfig.getParam("sequence", "warn") || atDiff > ModuleConfig.getParam("sequence", "warn")) return true;
		}
		return false;
	}

	public void makeReport(HTMLReportArchive report) throws IOException, XMLStreamException {
		if (!calculated) getPercentages();
		
		writeDefaultImage(report, "per_base_sequence_content.png", "Per base sequence content", Math.max(800, xCategories.length*15), 600);
		
		StringBuffer sb = report.dataDocument();
		sb.append("#Base\tG\tA\tT\tC\n");
		for (int i=0; i<xCategories.length; i++) {
			sb.append(xCategories[i]).append("\t")
			  .append(percentages[3][i]).append("\t")
			  .append(percentages[2][i]).append("\t")
			  .append(percentages[0][i]).append("\t")
			  .append(percentages[1][i]).append("\n");
		}
	}
}