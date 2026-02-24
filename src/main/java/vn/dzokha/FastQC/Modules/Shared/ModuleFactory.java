/**
 * Copyright Copyright 2014-17 Simon Andrews
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

import vn.dzokha.FastQC.Modules.Shared.BasicStats;
import vn.dzokha.FastQC.Modules.Shared.PerBaseQualityScores;
import vn.dzokha.FastQC.Modules.Shared.PerTileQualityScores;
import vn.dzokha.FastQC.Modules.Shared.PerSequenceQualityScores;
import vn.dzokha.FastQC.Modules.Shared.PerBaseSequenceContent;
import vn.dzokha.FastQC.Modules.Shared.PerSequenceGCContent;
import vn.dzokha.FastQC.Modules.Shared.NContent;
import vn.dzokha.FastQC.Modules.Shared.SequenceLengthDistribution;
import vn.dzokha.FastQC.Modules.Shared.OverRepresentedSeqs;
import vn.dzokha.FastQC.Modules.Adapter.AdapterContent; // Chú ý: File này nằm ở package Adapter
import vn.dzokha.FastQC.Modules.Kmer.KmerContent;       // Chú ý: File này nằm ở package Kmer



public class ModuleFactory {

    public static QCModule[] getStandardModuleList() {

        // Khởi tạo OverRepresentedSeqs trước vì nó cung cấp module DuplicationLevel
        OverRepresentedSeqs os = new OverRepresentedSeqs();
        
        QCModule[] module_list = new QCModule[] {
            new BasicStats(),
            new PerBaseQualityScores(),
            new PerTileQualityScores(),
            new PerSequenceQualityScores(),
            new PerBaseSequenceContent(),
            new PerSequenceGCContent(),
            new NContent(),
            new SequenceLengthDistribution(),
            os.duplicationLevelModule(), // Module tính toán mức độ lặp
            os,                          // Module trình tự xuất hiện nhiều
            new AdapterContent(),        // Module nội dung Adapter
            new KmerContent(),           // Module Kmer
        };
    
        return module_list;
    }
}
