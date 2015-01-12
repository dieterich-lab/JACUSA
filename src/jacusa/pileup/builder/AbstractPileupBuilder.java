package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.filter.samtag.SamTagFilter;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.process.phred2prob.Phred2Prob;
import jacusa.util.Coordinate;

import java.util.List;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMValidationError;

public abstract class AbstractPileupBuilder {

	// in genomic coordinates
	protected String contig;
	protected int genomicWindowStart;
	
	protected int windowSize;
	protected int maxGenomicPosition;

	protected SAMRecord[] SAMRecordsBuffer;
	protected SAMFileReader reader;

	protected int filteredSAMRecords;

	protected BaseConfig baseConfig;
	protected SampleParameters sample;
	
	protected boolean isCached;

	protected DefaultPileup pileup;

	public AbstractPileupBuilder(
			final Coordinate coordinate, 
			final SAMFileReader SAMFileReader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		contig				= coordinate.getSequenceName();
		genomicWindowStart 	= coordinate.getStart();
		this.windowSize 	= parameters.getWindowSize();
		maxGenomicPosition 	= Math.min(coordinate.getEnd(), SAMFileReader.getFileHeader().getSequence(contig).getSequenceLength());
		
		SAMRecordsBuffer	= new SAMRecord[30000];
		this.reader			= SAMFileReader;

		filteredSAMRecords	= 0;

		baseConfig			= parameters.getBaseConfig();
		this.sample			= sample;

		isCached			= false;

		pileup 				= new DefaultPileup(baseConfig.getBases().length);
	}

	/**
	 * 
	 * @param targetPosition
	 * @return
	 */
	public SAMRecord getNextValidRecord(int targetPosition) {
		SAMRecordIterator iterator = reader.query(contig, targetPosition, maxGenomicPosition, false);
		while (iterator.hasNext() ) {
			SAMRecord record = iterator.next();

			if (isValid(record)) {
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
	
	/**
	 * Tries to adjust to target position
	 * Return true if at least one valid SAMRecord could be found.
	 * WARNING: currentGenomicPosition != targetPosition is possible after method call 
	 * @param smallestTargetPosition
	 * @return
	 */
	public boolean adjustWindowStart(int genomicWindowStart) {
		isCached = false;
		this.genomicWindowStart = genomicWindowStart;
		clearCache();

		// get iterator to fill the window
		SAMRecordIterator iterator = reader.query(contig, this.genomicWindowStart, getWindowEnd(), false);

		// true if a valid read is found within genomicWindowStart and genomicWindowStart + windowSize
		boolean windowHit = false;
		int SAMReocordsInBuffer = 0;

		while (iterator.hasNext()) {
			SAMRecord record = iterator.next();

			if(isValid(record)) {
				SAMRecordsBuffer[SAMReocordsInBuffer++] = record;
			} else {
				filteredSAMRecords++;
			}

			// process buffer
			if (SAMReocordsInBuffer >= SAMRecordsBuffer.length) {
				for (SAMRecord bufferedRecord : SAMRecordsBuffer) {
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
			for (int i = 0; i < SAMReocordsInBuffer; ++i) {
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
	protected boolean isValid(SAMRecord samRecord) {
		int mapq = samRecord.getMappingQuality();
		List<SAMValidationError> errors = samRecord.isValid();

		if (! samRecord.getReadUnmappedFlag()
				&& ! samRecord.getNotPrimaryAlignmentFlag() // ignore non-primary alignments
				&& (mapq < 0 || mapq >= sample.getMinMAPQ()) // filter by mapping quality
				&& (sample.getFilterFlags() == 0 || (sample.getFilterFlags() > 0 && ((samRecord.getFlags() & sample.getFilterFlags()) == 0)))
				&& (sample.getRetainFlags() == 0 || (sample.getRetainFlags() > 0 && ((samRecord.getFlags() & sample.getRetainFlags()) > 0)))
				&& errors == null // isValid is expensive
				) { // only store valid records that contain mapped reads
			// custom filter 
			for (SamTagFilter samTagFilter : sample.getSamTagFilters()) {
				if (samTagFilter.filter(samRecord)) {
					return false;
				}
			}

			// no errors found
			return true;
		}

		// print error messages
		if (errors != null) {
			for (SAMValidationError error : errors) {
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
	public int getWindowEnd() {
		return Math.min(genomicWindowStart + windowSize - 1, maxGenomicPosition);
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
	public int getGenomicWindowStart() {
		return genomicWindowStart;
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	public int getGenomicPosition(int windowPosition) {
		return genomicWindowStart + windowPosition;
	}

	protected void processAlignmentBlock(final SAMRecord record, final AlignmentBlock alignmentBlock) {
		int readPosition = alignmentBlock.getReadStart() - 1; // 1-based index
		int genomicPosition = alignmentBlock.getReferenceStart();

		for (int offset = 0; offset < alignmentBlock.getLength(); ++offset) {
			final byte base = record.getReadBases()[readPosition + offset];
			byte qual = record.getBaseQualities()[readPosition + offset];

			// all probs are reset
			qual = (byte)Math.min(Phred2Prob.MAX_Q - 1, qual);

			if (qual >= sample.getMinBASQ()) {
				// speedup: if windowPosition == -1 the remaining part of the read will be outside of the windowCache
				// ignore the overhanging part of the read until it overlaps with the window cache
				final int windowPosition = convertGenomicPosition2WindowPosition(genomicPosition + offset);

				if (windowPosition == -1) {
					return;
				}
				if (windowPosition == -2) { // speedup jump to covered position
					offset += genomicWindowStart - genomicPosition - 1; // this should be negative 
				}
				if (windowPosition >= 0) {
					add2WindowCache(windowPosition, base, qual, record);
				}
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
		processFilters(record);
	}

	// abstract methods

	// Reset all caches in windows
	public abstract void clearCache();
	protected abstract void add2WindowCache(int windowPosition, byte base, byte qual, SAMRecord record);
	
	public abstract boolean isCovered(int windowPosition, STRAND strand);
	public abstract int getCoverage(int windowPosition, STRAND strand);

	public abstract Pileup getPileup(int windowPosition, STRAND strand);

	abstract public FilterContainer getFilterContainer(int windowPosition, STRAND strand);
	protected abstract void processFilters(final SAMRecord record) throws Exception;

}