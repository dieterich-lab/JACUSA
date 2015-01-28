package jacusa.pileup.builder;

import jacusa.JACUSA;
import jacusa.cli.options.sample.filter.samtag.SamTagFilter;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Coordinate;
import jacusa.util.WindowCoordinates;

import java.util.List;

import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMValidationError;

public abstract class AbstractPileupBuilder {

	// in genomic coordinates
	protected WindowCoordinates windowCoordinates;

	protected SAMRecord[] SAMRecordsBuffer;
	protected SAMFileReader reader;

	protected int filteredSAMRecords;

	protected BaseConfig baseConfig;
	protected SampleParameters sampleParameters;
	
	protected boolean isCached;

	protected WindowCache windowCache;
	protected FilterContainer filterContainer;
	protected int[] byte2int;
	protected STRAND strand;
	
	public AbstractPileupBuilder (
			final Coordinate coordinate,
			final STRAND strand, 
			final SAMFileReader SAMFileReader, 
			final SampleParameters sampleParameters,
			final AbstractParameters parameters) {

		// DIRTY hack
		final int sequenceLength = SAMFileReader.getFileHeader().getSequence(coordinate.getSequenceName()).getSequenceLength();
		if (coordinate.getEnd() > sequenceLength) {
			Coordinate samHeader = new Coordinate(coordinate.getSequenceName(), 1, sequenceLength);
			JACUSA.printWarning("Coordinates in BED file (" +  coordinate.toString() + ") do not fit to SAM sequence header (" + samHeader.toString()+ ").");
		}

		windowCoordinates		= new WindowCoordinates(
				coordinate.getSequenceName(), 
				coordinate.getStart(), 
				parameters.getWindowSize(), 
				coordinate.getEnd());

		SAMRecordsBuffer		= new SAMRecord[30000];
		reader					= SAMFileReader;

		filteredSAMRecords		= 0;

		baseConfig				= parameters.getBaseConfig();
		this.sampleParameters	= sampleParameters;

		isCached				= false;
		
		this.strand				= strand;
		
		windowCache 	= new WindowCache(windowCoordinates, baseConfig.getBaseLength());
		filterContainer	= parameters.getFilterConfig().createFilterContainer(windowCoordinates, sampleParameters);

		final BaseConfig baseConfig = parameters.getBaseConfig();
		byte2int = baseConfig.getByte2Int();
	}

	/**
	 * 
	 * @param targetPosition
	 * @return
	 */
	public SAMRecord getNextValidRecord(int targetPosition) {
		SAMRecordIterator iterator = reader.query(
				windowCoordinates.getContig(), 
				targetPosition, 
				windowCoordinates.getMaxGenomicPosition(), 
				false);
		
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
		clearCache();
		windowCoordinates.setGenomicWindowStart(genomicWindowStart);
		
		// get iterator to fill the window
		SAMRecordIterator iterator = reader.query(
				windowCoordinates.getContig(), 
				windowCoordinates.getGenomicWindowStart(), 
				windowCoordinates.getGenomicWindowEnd(), 
				false);

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
	 * Checks if a record fulfills user defined criteria
	 * @param samRecord
	 * @return
	 */
	protected boolean isValid(SAMRecord samRecord) {
		int mapq = samRecord.getMappingQuality();
		List<SAMValidationError> errors = samRecord.isValid();

		if (! samRecord.getReadUnmappedFlag()
				&& ! samRecord.getNotPrimaryAlignmentFlag() // ignore non-primary alignments CHECK
				&& (mapq < 0 || mapq >= sampleParameters.getMinMAPQ()) // filter by mapping quality
				&& (sampleParameters.getFilterFlags() == 0 || (sampleParameters.getFilterFlags() > 0 && ((samRecord.getFlags() & sampleParameters.getFilterFlags()) == 0)))
				&& (sampleParameters.getRetainFlags() == 0 || (sampleParameters.getRetainFlags() > 0 && ((samRecord.getFlags() & sampleParameters.getRetainFlags()) > 0)))
				&& errors == null // isValid is expensive
				) { // only store valid records that contain mapped reads
			// custom filter 
			for (SamTagFilter samTagFilter : sampleParameters.getSamTagFilters()) {
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
	 * 
	 * @return
	 */
	public int getFilteredSAMRecords() {
		return filteredSAMRecords;
	}

	public WindowCoordinates getWindowCoordinates() {
		return windowCoordinates;
	}

	// abstract methods

	// Reset all caches in windows
	public abstract void clearCache();
	protected abstract void add2WindowCache(int windowPosition, int base, int qual, STRAND strand);

	// strand dependet methods
	public abstract boolean isCovered(int windowPosition, STRAND strand);
	public abstract int getCoverage(int windowPosition, STRAND strand);
	public abstract Pileup getPileup(int windowPosition, STRAND strand);
	public abstract FilterContainer getFilterContainer(int windowPosition, STRAND strand);

	protected void processHardClipping(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record) {
		System.err.println("Hard Clipping not handled yet!");
	}
	
	protected void processSoftClipping(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record) {
		// override if needed
	}

	protected void processPadding(
			int windowPosition, 
			int readPosition, 
			int genomicPosition,
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		System.err.println("Padding not handled yet!");
	}
	
	protected void processRecord(SAMRecord record) {
		// init	
		int readPosition 	= 0;
		int genomicPosition = record.getAlignmentStart();
		int windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
		int alignmentBlockI = 0;
		
		int alignmentBlockLength[] = new int[record.getAlignmentBlocks().size() + 2];
		alignmentBlockLength[0] = 0;
		for (int i = 0; i < record.getAlignmentBlocks().size(); i++) {
			alignmentBlockLength[i + 1] = record.getAlignmentBlocks().get(i).getLength();
		}
		alignmentBlockLength[record.getAlignmentBlocks().size() + 1] = 0;

		for (AbstractFilterStorage<?> filter : filterContainer.getPR()) {
			filter.processRecord(windowCoordinates.getGenomicWindowStart(), record);
		}

		// process CIGAR -> SP, INDELs
		for (final CigarElement cigarElement : record.getCigar().getCigarElements()) {
			
			switch(cigarElement.getOperator()) {

			/*
			 * handle insertion
			 */
			case I:
				processInsertion(
						windowPosition, 
						readPosition, 
						genomicPosition, 
						alignmentBlockLength[alignmentBlockI], 
						alignmentBlockLength[alignmentBlockI + 1], 
						cigarElement, 
						record);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * handle alignment/sequence match and mismatch
			 */
			case M:
			case EQ:
			case X:
				processAlignmentMatch(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				genomicPosition += cigarElement.getLength();
				windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
				alignmentBlockI++;
				break;

			/*
			 * handle hard clipping 
			 */
			case H:
				processHardClipping(windowPosition, readPosition, genomicPosition, cigarElement, record);
				break;

			/*
			 * handle deletion from the reference and introns
			 */
			case D:
				processDeletion(
						windowPosition, 
						readPosition, 
						genomicPosition, 
						alignmentBlockLength[alignmentBlockI], 
						alignmentBlockLength[alignmentBlockI + 1],
						cigarElement, record);
				genomicPosition += cigarElement.getLength();
				windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
				break;

			case N:
				processSkipped(
						windowPosition, 
						readPosition, 
						genomicPosition, 
						alignmentBlockLength[alignmentBlockI], 
						alignmentBlockLength[alignmentBlockI + 1],
						cigarElement, record);
				genomicPosition += cigarElement.getLength();
				windowPosition  = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition);
				break;

			/*
			 * soft clipping
			 */
			case S:
				processSoftClipping(windowPosition, readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * silent deletion from padded sequence
			 */
			case P:
				processPadding(
						windowPosition, 
						readPosition, 
						genomicPosition,
						alignmentBlockLength[alignmentBlockI], 
						alignmentBlockLength[alignmentBlockI + 1],
						cigarElement, 
						record);
				break;

			default:
				throw new RuntimeException("Unsupported Cigar Operator: " + cigarElement.getOperator().toString());
			}
		}
	}

	protected void processAlignmentMatch(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			CigarElement cigarElement, 
			SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
			// TODO
			filter.processAlignmentBlock(windowPosition, readPosition, genomicPosition, cigarElement, record);
		}

		for (int offset = 0; offset < cigarElement.getLength(); ++offset) {
			final int baseI = byte2int[record.getReadBases()[readPosition + offset]];

			int qual = record.getBaseQualities()[readPosition + offset];
			// all probs are reset
			qual = Math.min(Phred2Prob.MAX_Q - 1, qual);

			if (baseI >= 0 && qual >= sampleParameters.getMinBASQ()) {
				// speedup: if windowPosition == -1 the remaining part of the read will be outside of the windowCache
				// ignore the overhanging part of the read until it overlaps with the window cache
				windowPosition = windowCoordinates.convertGenomicPosition2WindowPosition(genomicPosition + offset);

				if (windowPosition == -1) {
					// TODO parse some filters
					return;
				}
				if (windowPosition == -2) { // speedup jump to covered position
					offset += windowCoordinates.getGenomicWindowStart() - (genomicPosition + offset) - 1; // this should be negative 
				}
				if (windowPosition >= 0) {
					add2WindowCache(windowPosition, baseI, qual, strand);
					for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
						filter.processAlignmentMatch(windowPosition, readPosition + offset, genomicPosition + offset, record, baseI, qual);
					}
				}
			}
		}
	}

	protected void processInsertion(
			int windowPosition, 
			int readPosition, 
			int genomicPosition,
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.I)) {
			filter.processInsertion(
					windowPosition, 
					readPosition, 
					genomicPosition, 
					upstreamMatch, 
					downstreamMatch, 
					cigarElement, 
					record);
		}
	}

	protected void processDeletion(
			int windowPosition, 
			int readPosition, 
			int genomicPosition, 
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.D)) {
			filter.processDeletion(
					windowPosition, 
					readPosition, 
					genomicPosition, 
					upstreamMatch,
					downstreamMatch,
					cigarElement, 
					record);
		}
	}

	protected void processSkipped(
			int windowPosition, 
			int readPosition, 
			int genomicPosition,
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.N)) {
			filter.processSkipped(
					windowPosition, 
					readPosition, 
					genomicPosition,
					upstreamMatch,
					downstreamMatch,
					cigarElement, 
					record);
		}
	}

}