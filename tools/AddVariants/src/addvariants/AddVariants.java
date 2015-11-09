/*
  	AddVariants implants variants into BAM files.
    Copyright (C) 2015  Michael Piechotta

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package addvariants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMTextWriter;

public class AddVariants {

	private String bamPathname;
	private String bedPathname;

	private SAMTextWriter samTextWriter;

	private Random random;

	private final String mutationCountAttribute = "ZM";
	
	public AddVariants(String bamPathname, String bedPathname) {
		this.bamPathname = bamPathname;
		this.bedPathname = bedPathname;

		samTextWriter = new SAMTextWriter(System.out);
		random = new Random();
	}

	private void run() throws Exception {
		File samFile = new File(bamPathname);
		
		// SAM records
		SAMFileReader samFileReader = new SAMFileReader(samFile);
		SAMRecordIterator samRecordIterator = samFileReader.iterator();

		samTextWriter.setHeader(samFileReader.getFileHeader());

		// variants
		VariantIterator variantIt = new VariantIterator(bedPathname);
		List<Variant> variants = new ArrayList<Variant>(4); 

		while(samRecordIterator.hasNext()) {
			SAMRecord record = samRecordIterator.next();

			if(variantIt.hasNext()) {
				Variant variant = variantIt.getNext();
				
				// fast forward variants to match SAMrecord
				int chrOrder = getChrOrder(samFileReader, record, variant);
				if(chrOrder < 0) {
					// chr lagging
				} else {
					// variants laggin
					while(chrOrder > 0) {
						variant = variantIt.getNext();
						chrOrder = getChrOrder(samFileReader, record, variant);
						if(chrOrder > 0) {
							variant = variantIt.next();
							if(!variantIt.hasNext()) {
								break;
							}
						}
					}

					if(chrOrder == 0) {
						int positionOrder = getPositionOrder(record, variant);
						
						while(chrOrder == 0 && positionOrder == 0) {
							variant = variantIt.getNext();
							chrOrder = getChrOrder(samFileReader, record, variant);
							positionOrder = getPositionOrder(record, variant);
							if(chrOrder == 0 && positionOrder == 0) {
								variant = variantIt.next();
								variants.add(variant);
								if(!variantIt.hasNext()) {
									break;
								}
							}
						}
					}
				}
			}

			List<Variant> variantsTmp = new ArrayList<Variant>(variants.size()); 
			// prune not overlapping 
			for(Variant variant : variants) {
				// same chr and variant not upstream
				if(getChrOrder(samFileReader, record, variant) == 0 && getPositionOrder(record, variant) != 1) {
					variantsTmp.add(variant);
				}
			}
			variants = variantsTmp;

			// add all the variants to the record
			for(Variant variant : variants) {
				// but only those that are "covered" by the read: -> spliced alignment
				int readPosition = getReadPosition(record, variant);
				if(readPosition >= 0 && readPosition < record.getReadLength()) {
					mutate(record, readPosition, variant);
				}
			}
 
			write(record);
		}

		// close and finish
		samRecordIterator.close();
		samFileReader.close();
		variantIt.close();

		samTextWriter.finish();
	}

	/**
	 * Mutate record at readPosition with variant
	 * @param record
	 * @param readPosition
	 * @param variant
	 */
	private void mutate(SAMRecord record, int readPosition, Variant variant) {
		double ratio = variant.getRatio();				
		if(random.nextDouble() <= ratio) {
			byte[] readBases = record.getReadBases();
			if(record.getIntegerAttribute(mutationCountAttribute) == null) {
				record.setAttribute(mutationCountAttribute, 0);
			}
			int count = record.getIntegerAttribute(mutationCountAttribute);

			count++;
			readBases[readPosition] = (byte)variant.getBase();
			record.setReadBases(readBases);
			record.setAttribute(mutationCountAttribute, count);
		}
	}

	/**
	 * Get ReadPosition of variant in read
	 * @param record
	 * @param variant
	 * @return
	 */
	private int getReadPosition(SAMRecord record, Variant variant) {
		int readPosition = -1;
		for(AlignmentBlock alignmentBlock : record.getAlignmentBlocks()) {
			if(alignmentBlock.getReferenceStart() <= variant.getStart() + 1 && variant.getStart() + 1 <= alignmentBlock.getReferenceStart() + alignmentBlock.getLength()) {
				readPosition = variant.getStart() + 1 - alignmentBlock.getReferenceStart() + alignmentBlock.getReadStart() - 1;
				break;
			}
		}
		
		return readPosition;
	}
	
	private void write(SAMRecord record) {
		samTextWriter.writeAlignment(record);
	}

	/**
	 * -1 Variant downstream
	 *  0 Variant contained
	 *  1 Variant upstream
	 *  in read alignment
	 * @param record
	 * @param variant
	 * @return
	 */
	private int getPositionOrder(SAMRecord record, Variant variant) {
		if(record.getAlignmentEnd() < variant.getStart() + 1) {
			return -1;
		}
		
		if(variant.getStart() + 1 < record.getAlignmentStart()) {
			return 1;
		}

		return 0;
	}

	/**
	 * <0 Chr lagging
	 *  0 same chr
	 * >0 Variant lagging
	 * @param samFileReader
	 * @param record
	 * @param variant
	 * @return
	 */
	private int getChrOrder(SAMFileReader samFileReader, SAMRecord record, Variant variant) {
		// index in sequence header
		int i = samFileReader.getFileHeader().getSequenceIndex(record.getReferenceName());
		int j = samFileReader.getFileHeader().getSequenceIndex(variant.getContig());
		return i - j;
	}
	
	private static void printUsage() {
		System.err.println("usage: <bam> <bed>");
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			printUsage();
			return;
		}

		AddVariants addVarians = new AddVariants(args[0], args[1]);
		addVarians.run();
	}

}
