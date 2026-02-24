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
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import vn.dzokha.FastQC.Modules.Graphs.BaseGroup;
import vn.dzokha.FastQC.Modules.Graphs.TileGraph;
import vn.dzokha.FastQC.Modules.Report.HTMLReportArchive;
import vn.dzokha.FastQC.Modules.Sequence.Sequence;
import vn.dzokha.FastQC.Modules.Sequence.QualityEncoding.PhredEncoding;
import vn.dzokha.FastQC.Modules.Utilities.QualityCount;

/**
 * Phân tích chất lượng dữ liệu theo từng Tile (ô) trên Flowcell.
 */
public class PerTileQualityScores extends AbstractQCModule {

	public HashMap<Integer, QualityCount []> perTileQualityCounts = new HashMap<Integer, QualityCount[]>();
	private int currentLength = 0;
	private double [][] means = null;
	private String [] xLabels;
	private int [] tiles;
	private int high = 0;
	private PhredEncoding encodingScheme;
	private boolean calculated = false;
	private long totalCount = 0;
	private int splitPosition = -1;
	private double maxDeviation = 0;
	private boolean ignoreInReport = false;
	private TileGraph graph;

	@Override
	public Object getResultsPanel() {
		if (!calculated) getPercentages();
		if (means == null) return null;

		// Khởi tạo TileGraph (Headless version)
		graph = new TileGraph(xLabels, tiles, means);
		return graph;
	}

	public boolean ignoreFilteredSequences() {
		return true;
	}

	public boolean ignoreInReport () {
		if (ignoreInReport || ModuleConfig.getParam("tile", "ignore") > 0 || currentLength == 0) {
			return true;
		}
		return false;
	}

	private synchronized void getPercentages () {
		if (perTileQualityCounts.isEmpty()) {
			calculated = true;
			return;
		}

		char [] range = calculateOffsets();
		encodingScheme = PhredEncoding.getFastQEncodingOffset(range[0]);
		high = range[1] - encodingScheme.offset();
		if (high < 35) high = 35;

		BaseGroup [] groups = BaseGroup.makeBaseGroups(currentLength);
		Integer [] tileNumbers = perTileQualityCounts.keySet().toArray(new Integer[0]);
		Arrays.sort(tileNumbers);
		
		tiles = new int[tileNumbers.length];
		for (int i=0; i<tiles.length; i++) {
			tiles[i] = tileNumbers[i];
		}
		
		means = new double[tileNumbers.length][groups.length];
		xLabels = new String[groups.length];

		for (int t=0; t<tileNumbers.length; t++){
			for (int i=0; i<groups.length; i++) {
				if (t == 0) xLabels[i] = groups[i].toString();

				int minBase = groups[i].lowerCount();
				int maxBase = groups[i].upperCount();
				means[t][i] = getMean(tileNumbers[t], minBase, maxBase, encodingScheme.offset());
			}
		}
		
		// Chuẩn hóa dữ liệu (Normalisation)
		double currentMaxDev = 0;
		double [] averageQualitiesPerGroup = new double[groups.length];
		
		for (int t=0; t<tileNumbers.length; t++) {		
			for (int i=0; i<groups.length; i++) {
				averageQualitiesPerGroup[i] += means[t][i];
			}
		}
		
		for (int i=0; i<averageQualitiesPerGroup.length; i++) {
			averageQualitiesPerGroup[i] /= tileNumbers.length;
		}

		for (int i=0; i<groups.length; i++) {
			for (int t=0; t<tileNumbers.length; t++) {
				means[t][i] -= averageQualitiesPerGroup[i];
				if (Math.abs(means[t][i]) > currentMaxDev) {
					currentMaxDev = Math.abs(means[t][i]);
				}
			}
		}
		
		this.maxDeviation = currentMaxDev;
		calculated = true;
	}

	private char [] calculateOffsets () {
		char minChar = 0;
		char maxChar = 0;

		for (QualityCount[] qualityCounts : perTileQualityCounts.values()) {
			for (QualityCount qc : qualityCounts) {
				if (minChar == 0) {
					minChar = qc.getMinChar();
					maxChar = qc.getMaxChar();
				} else {
					if (qc.getMinChar() < minChar && qc.getMinChar() != 0) minChar = qc.getMinChar();
					if (qc.getMaxChar() > maxChar) maxChar = qc.getMaxChar();
				}
			}
		}
		return new char[] {minChar, maxChar};
	}

	public void processSequence(Sequence sequence) {
		if (totalCount == 0 && ModuleConfig.getParam("tile", "ignore") > 0) {
			ignoreInReport = true;
		}

		if (ignoreInReport || sequence.getQualityString().length() == 0) return;
				
		calculated = false;
		totalCount++;
		
		// Sampling: Lấy 10k seq đầu tiên, sau đó lấy 10%
		if (totalCount > 10000 && totalCount % 10 != 0) return;
		
		int tile = -1;
		String [] splitID = sequence.getID().split(":");

		try {
			if (splitPosition >= 0) {
				if (splitID.length > splitPosition) {
					tile = Integer.parseInt(splitID[splitPosition]);
				}
			} else if (splitID.length >= 7) {
				splitPosition = 4;
				tile = Integer.parseInt(splitID[4]);
			} else if (splitID.length >= 5) {
				splitPosition = 2;
				tile = Integer.parseInt(splitID[2]);
			} else {
				ignoreInReport = true;
				return;
			}
		} catch (NumberFormatException nfe) {
			ignoreInReport = true;
			return;
		}

		if (tile == -1) return;

		char [] qual = sequence.getQualityString().toCharArray();
		if (currentLength < qual.length) {
			expandQualityCounts(qual.length);
			currentLength = qual.length;
		}

		if (!perTileQualityCounts.containsKey(tile)) {
			if (perTileQualityCounts.size() > 2500) {
				ignoreInReport = true;
				perTileQualityCounts.clear();
				return;
			}
			QualityCount [] qualityCounts = new QualityCount[currentLength];
			for (int i=0; i<currentLength; i++) qualityCounts[i] = new QualityCount();
			perTileQualityCounts.put(tile, qualityCounts);
		}

		QualityCount [] qualityCounts = perTileQualityCounts.get(tile);
		for (int i=0; i<qual.length; i++) {
			qualityCounts[i].addValue(qual[i]);
		}
	}

	private void expandQualityCounts(int newLength) {
		for (Integer thisTile : perTileQualityCounts.keySet()) {
			QualityCount [] oldCounts = perTileQualityCounts.get(thisTile);
			QualityCount [] newCounts = new QualityCount[newLength];
			System.arraycopy(oldCounts, 0, newCounts, 0, oldCounts.length);
			for (int i=oldCounts.length; i<newLength; i++) {
				newCounts[i] = new QualityCount();				
			}
			perTileQualityCounts.put(thisTile, newCounts);
		}
	}

	public void reset () {
		totalCount = 0;
		perTileQualityCounts.clear();
		calculated = false;
		currentLength = 0;
		splitPosition = -1;
	}

	public String description() {
		return "Shows the per tile quality scores of all bases at a given position in a sequencing run";
	}

	public String name() {
		return "Per tile sequence quality";
	}

	public boolean raisesError() {
		if (!calculated) getPercentages();
		return maxDeviation > ModuleConfig.getParam("tile", "error");
	}

	public boolean raisesWarning() {
		if (!calculated) getPercentages();
		return maxDeviation > ModuleConfig.getParam("tile", "warn");
	}

	public void makeReport(HTMLReportArchive report) throws IOException, XMLStreamException {
		if (!calculated) getPercentages();
		
		writeDefaultImage(report, "per_tile_quality.png", "Per tile quality graph", Math.max(800, xLabels.length*15), 600);

		// SỬA LỖI TẠI ĐÂY: Thay StringBuilder bằng StringBuffer
		StringBuffer sb = report.dataDocument();
		sb.append("#Tile\tBase\tMean\n");
		for (int t=0; t<tiles.length; t++) {
			for (int i=0; i<means[t].length; i++) {
				sb.append(tiles[t]).append("\t")
				  .append(xLabels[i]).append("\t")
				  .append(means[t][i]).append("\n");
			}
		}
	}

	private double getMean (int tile, int minbp, int maxbp, int offset) {
		int count = 0;
		double total = 0;
		QualityCount [] qualityCounts = perTileQualityCounts.get(tile);
		if (qualityCounts == null) return 0;

		for (int i=minbp-1; i<maxbp; i++) {
			if (i < qualityCounts.length && qualityCounts[i].getTotalCount() > 0) {
				count++;
				total += qualityCounts[i].getMean(offset);
			}
		}
		return count > 0 ? total/count : 0;
	}
}