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

package vn.dzokha.FastQC.Modules.Sequence;

import org.springframework.stereotype.Service;
import vn.dzokha.FastQC.Config.FastQCConfig; // Giả sử bạn dùng lớp cấu hình mới
import java.io.IOException;
import java.io.InputStream;



@Service 
public class SequenceFactory {

    private final FastQCConfig config;

    public SequenceFactory(FastQCConfig config) {
        this.config = config;
    }

    public SequenceFile getSequenceFile(String fileName, InputStream inputStream) throws SequenceFormatException, IOException {
        
        String format = config.getSequenceFormat();

        // 1. Kiểm tra định dạng dựa trên cấu hình (Explicit format)
        if (format != null) {
            if (format.equalsIgnoreCase("bam") || format.equalsIgnoreCase("sam")) {
                return new BAMFile(inputStream, fileName, false);
            }
            if (format.equalsIgnoreCase("fastq")) {
                return new FastQFile(config, inputStream, fileName);
            }
            throw new SequenceFormatException("Unsupported format: " + format);
        }

        // 2. Tự động nhận diện dựa trên đuôi file (Auto-detect)
        String lowerName = fileName.toLowerCase();
        
        if (lowerName.endsWith(".bam") || lowerName.endsWith(".sam")) {
            return new BAMFile(inputStream, fileName, false);
        }
        
        if (lowerName.endsWith(".fast5")) {
            return new Fast5File(inputStream, fileName);
        }

        // Mặc định trả về FastQ
        return new FastQFile(config, inputStream, fileName);
    }
}
	
	