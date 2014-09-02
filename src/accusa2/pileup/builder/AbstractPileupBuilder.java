package accusa2.pileup.builder;

import java.util.List;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMValidationError;
import accusa2.cli.parameters.Parameters;
import accusa2.filter.samtag.SamTagFilter;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.Pileup;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractPileupBuilder {

	// in genomic coordinates
	protected String contig;
	protected int genomicWindowStart;
	protected int windowSize;
	protected int maxGenomicPosition;

	protected SAMRecord[] SAMRecordsBuffer;
	protected SAMFileReader reader;

	protected int filteredSAMRecords;

	protected Parameters parameters;
	
	protected boolean isCached;

	protected DefaultPileup pileup;
	
	public AbstractPileupBuilder(AnnotatedCoordinate annotatedCoordinate, SAMFileReader SAMFileReader, int windowSize, Parameters parameters) {
		contig				= annotatedCoordinate.getSequenceName();
		genomicWindowStart 	= annotatedCoordinate.getStart();
		this.windowSize 	= windowSize;
		maxGenomicPosition 	= Math.min(annotatedCoordinate.getEnd(), SAMFileReader.getFileHeader().getSequence(contig).getSequenceLength());

		SAMRecordsBuffer	= new SAMRecord[30000];
		this.reader			= SAMFileReader;

		filteredSAMRecords	= 0;

		this.parameters		= parameters;

		isCached			= false;

		pileup 				= new DefaultPileup();
	}

	/**
	 * 
	 * @param targetPosition
	 * @return
	 */
	public SAMRecord getNextValidRecord(int targetPosition) {
		SAMRecordIterator iterator = reader.query(contig, targetPosition, maxGenomicPosition, false);
		while(iterator.hasNext() ) {
			SAMRecord record = iterator.next();

			if(isValid(record)) {
				iterator.close();
				iterator = null;
				return record;
			}
		}
		iterator.close();

		// if no more reads are found 
		return null;
	}

	public boolean isCached() {
		return isCached;
	}

	public boolean adjustWindowStart() {
		return adjustWindowStart(getWindowEnd());
	}
	
	/**
	 * Tries to adjust to target position
	 * Return true if at least one valid SAMRecord could be found.
	 * WARNING: currentGenomicPosition != targetPosition is possible after method call 
	 * @param smallestTargetPosition
	 * @return
	 */
	public boolean adjustWindowStart(int genomicWindowStart) {
		clearCache();
		isCached = false;
		this.genomicWindowStart = genomicWindowStart;

		// get iterator to fill the window
		SAMRecordIterator iterator = reader.query(contig, genomicWindowStart, Math.min(getWindowEnd(), maxGenomicPosition), false);

		// true if a valid read is found within genomicWindowStart and genomicWindowStart + windowSize
		boolean windowHit = false;
		int SAMReocordsInBuffer = 0;
		while(iterator.hasNext()) {
			SAMRecord record = iterator.next();

			if(isValid(record)) {
				SAMRecordsBuffer[SAMReocordsInBuffer++] = record;
			} else {
				filteredSAMRecords++;
			}

			// process buffer
			if (SAMReocordsInBuffer >= SAMRecordsBuffer.length) {
				for(SAMRecord bufferedRecord : SAMRecordsBuffer) {
					try {
						processRecord(bufferedRecord);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// reset counter
				SAMReocordsInBuffer = 0;
				// we found at least a valid SAMRecord
				windowHit = true;
			}
		}
		iterator.close();

		if (! windowHit && SAMReocordsInBuffer == 0) {
			// no reads found
			isCached = false;
			return false;
		} else { // process any left SAMrecords in the buffer
			for(int i = 0; i < SAMReocordsInBuffer; ++i) {
				try {
					processRecord(SAMRecordsBuffer[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			isCached = true;
			return true;
		}
	}

	/**
	 * 
	 * @param genomicPosition
	 * @return
	 */
	public boolean isContainedInGenome(int genomicPosition) {
		return genomicPosition <= maxGenomicPosition && genomicPosition > 0;
	}

	/**
	 * 
	 * @param genomicPosition
	 * @return
	 */
	public boolean isContainedInWindow(int genomicPosition) {
		return genomicPosition >= genomicWindowStart && genomicPosition <= getWindowEnd();
	}

	/**
	 * Checks if a record fulfills user defined criteria
	 * @param samRecord
	 * @return
	 */
	// TODO collect statistics
	protected boolean isValid(SAMRecord samRecord) {
		int mapq = samRecord.getMappingQuality();
		List<SAMValidationError> errors = samRecord.isValid();

		if(!samRecord.getReadUnmappedFlag()
				&& !samRecord.getNotPrimaryAlignmentFlag() // ignore non-primary alignments
				&& (mapq < 0 || mapq >= parameters.getMinMAPQ()) // filter by mapping quality
				&& (parameters.getFilterFlags() == 0 || (parameters.getFilterFlags() > 0 && ((samRecord.getFlags() & parameters.getFilterFlags()) == 0)))
				&& (parameters.getRetainFlags() == 0 || (parameters.getRetainFlags() > 0 && ((samRecord.getFlags() & parameters.getRetainFlags()) > 0)))
				&& errors == null // isValid is expensive
				) { // only store valid records that contain mapped reads
			// custom filter 
			for(SamTagFilter samTagFilter : parameters.getSamTagFilter()) {
				if(samTagFilter.filter(samRecord)) {
					return false;
				}
			}

			// no errors found
			return true;
		}

		// print error messages
		if(errors != null) {
			for(SAMValidationError error : errors) {
				 System.err.println(error.toString());
			}
		}

		// something went wrong
		return false;
	}
	
	/**
	 * Calculates genomicPosition or -1 or -2 if genomicPosition is outside the window
	 * -1 if downstream of windowEnd
	 * -2 if upstream of windowStart
	 * @param genomicPosition
	 * @return
	 */
	public int convertGenomicPosition2WindowPosition(int genomicPosition) {
		if(genomicPosition < genomicWindowStart) {
			return -2;
		} else if(genomicPosition > getWindowEnd()){
			return -1;
		}

		return genomicPosition - genomicWindowStart;
	}

	/**
	 * End of window (inclusive)
	 * @return
	 */
	protected int getWindowEnd() {
		return genomicWindowStart + windowSize - 1;
	}

	/**
	 * 
	 * @return
	 */
	public int getFilteredSAMRecords() {
		return filteredSAMRecords;
	}

	/**
	 * 
	 * @return
	 */
	public Parameters getParameters() {
		return parameters;
	}

	/**
	 * 
	 * @return
	 */
	public int getGenomicWindowStart() {
		return genomicWindowStart;
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	public int getCurrentGenomicPosition(int windowPosition) {
		return genomicWindowStart + windowPosition;
	}

	protected void processAlignmentBlock(final SAMRecord record, final AlignmentBlock alignmentBlock) {
		int readPosition = alignmentBlock.getReadStart() - 1;
		int genomicPosition = alignmentBlock.getReferenceStart();

		for (int offset = 0; offset < alignmentBlock.getLength(); ++offset) {
			final int baseI = parameters.getBaseConfig().getBaseI(record.getReadBases()[readPosition + offset]);
			final byte qual = record.getBaseQualities()[readPosition + offset];

			if(qual >= parameters.getMinBASQ() && baseI != -1) {
				// speedup: if windowPosition == -1 the remaining part of the read will be outside of the windowCache
				// ignore the overhanging part of the read until it overlaps with the window cache
				final int windowPosition = convertGenomicPosition2WindowPosition(genomicPosition + offset);
				if (windowPosition < 0) {
					return;
				}

				add2Cache(windowPosition, baseI, qual, record);
			}
		}
	}
	
	/**
	 * 
	 * @param record
	 * @throws Exception
	 */
	protected void processRecord(final SAMRecord record) throws Exception {
		// process alignment block
		for (AlignmentBlock alignmentBlock : record.getAlignmentBlocks()) {
			processAlignmentBlock(record, alignmentBlock);
		}

		// collect filter information
		processFilterCache(record);
	}

	// abstract methods

	// Reset all caches in windows
	public abstract void clearCache();
	public abstract boolean isCovered(int windowPosition, STRAND strand);
	public abstract Pileup getPileup(int windowPosition, STRAND strand);
	public abstract Counts[] getFilteredCounts(int windowPosition, STRAND strand);
	public abstract int getCoverage(int windowPosition, STRAND strand);
	protected abstract void add2Cache(int windowPosition, int baseI, byte qual, SAMRecord record);
	protected abstract void processFilterCache(final SAMRecord record) throws Exception;

}