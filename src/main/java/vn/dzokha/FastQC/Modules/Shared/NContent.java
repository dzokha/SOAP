/**
 * Copyright Copyright 2010-17 Simon Andrews
 *
 *    This file is part of FastQC.
 *
 *    FastQC is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    FastQC is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with FastQC; if not, write to the Free Software
 *    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package vn.dzokha.FastQC.Modules.Shared;

import java.io.IOException;

import javax.swing.JPanel;
import javax.xml.stream.XMLStreamException;

import vn.dzokha.FastQC.Modules.Graphs.BaseGroup;
import vn.dzokha.FastQC.Modules.Graphs.LineGraph;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;

public class NContent extends AbstractQCModule {

	public long [] nCounts = new long [0];
	public long [] notNCounts = new long [0];
	public boolean calculated = false;
	public double [] percentages = null;
	public String [] xCategories = new String[0];
	private LineGraph graph; // THÊM DÒNG NÀY
	
	// public Object getResultsPanel() {
		
	// 	if (!calculated) getPercentages();
	// 	return new LineGraph(new double [][] {percentages}, 0d, 100d, "Position in read (bp)",new String [] {"%N"}, xCategories, "N content across all bases");
	// }

	// @Override
    // public Object getResultsPanel() { // Đổi JPanel thành Object
    //     if (!calculated) getPercentages();
        
    //     double [][] data = new double[1][];
    //     data[0] = percentages;
    //     String [] titles = new String [] {"N Content"};
        
    //     graph = new LineGraph(data, 0, 100, "Position in read (bp)", titles, xCategories, "N content across all bases");
    //     return graph;
    // }

    @Override
    public Object getResultsPanel() {
        if (!calculated) getPercentages();
        double [][] data = new double[1][];
        data[0] = percentages;
        String [] titles = new String [] {"N Content"};
        graph = new LineGraph(data, 0, 100, "Position in read (bp)", titles, xCategories, "N content across all bases");
        return graph;
    }

	public boolean ignoreFilteredSequences() {
		return true;
	}
	
	public boolean ignoreInReport () {
		if (ModuleConfig.getParam("n_content", "ignore") > 0) {
			return true;
		}
		return false;
	}
	
	private synchronized void getPercentages () {
		
		BaseGroup [] groups = BaseGroup.makeBaseGroups(nCounts.length);
		
		xCategories = new String[groups.length];

		percentages = new double [groups.length];

		long total;
		long nCount;

		for (int i=0;i<groups.length;i++) {
						
			xCategories[i] = groups[i].toString();

			nCount = 0;
			total = 0;
			
			for (int bp=groups[i].lowerCount()-1;bp<groups[i].upperCount();bp++) {		
				nCount += nCounts[bp];
				total += nCounts[bp];
				total += notNCounts[bp];
			}
			
			percentages[i] = 100*(nCount/(double)total);
		}
				
		calculated = true;
		
	}
		
	public void processSequence(Sequence sequence) {
		calculated = false;
		char [] seq = sequence.getSequence().toCharArray();
		if (nCounts.length < seq.length) {
			// We need to expand the size of the data structures
			
			long [] nCountsNew = new long [seq.length];
			long [] notNCountsNew = new long [seq.length];

			for (int i=0;i<nCounts.length;i++) {
				nCountsNew[i] = nCounts[i];
				notNCountsNew[i] = notNCounts[i];
			}
			
			nCounts = nCountsNew;
			notNCounts = notNCountsNew;
		}
		
		for (int i=0;i<seq.length;i++) {
			if (seq[i] == 'N') {
				++nCounts[i];
			}
			else {
				++notNCounts[i];
			}
		}
		
	}
	
	
	public String name() { return "Per base N content"; }
    public String description() { return "Shows the percentage of bases at each position which are not being called"; }
    public boolean raisesError() { return false; }
    public boolean raisesWarning() { return false; }
    public void reset() { nCounts = new long[0]; notNCounts = new long[0]; calculated = false; }
	
	@Override
	public void makeReport(HTMLReportArchive report) throws XMLStreamException,IOException {
		if (!calculated) getPercentages();
		
        writeDefaultImage(report, "per_base_n_content.png", "Per base N content", Math.max(800, xCategories.length * 15), 600);
		
		StringBuffer sb = report.dataDocument();
		sb.append("#Base\tN-Count\n");
		for (int i=0;i<xCategories.length;i++) {
			sb.append(xCategories[i]);
			sb.append("\t");
			sb.append(percentages[i]);
			sb.append("\n");
		}
	}

}
