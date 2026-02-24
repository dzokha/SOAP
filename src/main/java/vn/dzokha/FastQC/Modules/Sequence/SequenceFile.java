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

import vn.dzokha.FastQC.Modules.Sequence.Sequence; // Thay bằng package thực tế của lớp Sequence
import vn.dzokha.FastQC.Modules.Sequence.SequenceFormatException;

import java.io.InputStream;
import java.io.IOException;

public interface SequenceFile extends AutoCloseable {

	public boolean hasNext();
	public Sequence next() throws SequenceFormatException;

	public String name();
	String getId();

	public boolean isColorspace();
	public int getPercentComplete();

    InputStream getInputStream() throws IOException;

    @Override
    void close() throws IOException;
	
}