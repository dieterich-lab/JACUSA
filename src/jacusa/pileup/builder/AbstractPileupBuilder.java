package jacusa.pileup.builder;

import jacusa.JACUSA;
import jacusa.cli.options.sample.filter.samtag.SamTagFilter;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Coordinate;
import jacusa.util.WindowCoordinates;

import java.util.Arrays;
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
	protected AbstractParameters parameters;
	
	protected boolean isCached;

	protected WindowCache windowCache;
	
	protected FilterContainer filterContainer;
	protected int[] byte2int;
	protected STRAND strand;

	protected int distance;
	
	public AbstractPileupBuilder (
			final Coordinate coordinate,
			final STRAND strand, 
			final SAMFileReader SAMFileReader, 
			final SampleParameters sampleParameters,
			final AbstractParameters parameters) {

		// check if coordinates are fine
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

		SAMRecordsBuffer		= new SAMRecord[20000];
		reader					= SAMFileReader;

		filteredSAMRecords		= 0;

		baseConfig				= parameters.getBaseConfig();
		this.sampleParameters	= sampleParameters;
		this.parameters			= parameters;

		isCached				= false;

		windowCache				= new WindowCache(windowCoordinates, baseConfig.getBaseLength());
		filterContainer			= parameters.getFilterConfig().createFilterContainer(windowCoordinates, strand, sampleParameters);
		byte2int 				= parameters.getBaseConfig().getByte2Int();
		this.strand				= strand;

		// get max overhang
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
			distance = Math.max(filter.getDistance(), distance);
		}
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
	protected abstract void addHighQualityBaseCall(int windowPosition, int base, int qual, STRAND strand);
	protected abstract void addLowQualityBaseCall(int windowPosition, int base, int qual, STRAND strand);

	// strand dependent methods
	public abstract boolean isCovered(int windowPosition, STRAND strand);
	public abstract int getCoverage(int windowPosition, STRAND strand);
	public abstract Pileup getPileup(int windowPosition, STRAND strand);
	public abstract WindowCache getWindowCache(STRAND strand);

	public abstract FilterContainer getFilterContainer(int windowPosition, STRAND strand);
	
	
	/*
	 * process CIGAR string methods
	 */
	
	protected void processHardClipping(
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record) {
		// System.err.println("Hard Clipping not handled yet!");
	}
	
	protected void processSoftClipping(
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record) {
		// override if needed
	}

	protected void processPadding(
			int readPosition, 
			int genomicPosition,
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		System.err.println("Padding not handled yet!");
	}

	protected byte[] parseMDField(final SAMRecord record) {
		String tag = "MD";
		Object o = record.getAttribute(tag);
		if (o == null) {
			throw new IllegalStateException("No MD field present for SAMRecord: " + record.toString());
		}

		// init container size with read length
		final byte[] referenceBases = new byte[record.getReadLength()];
		int destPos = 0;
		// copy read sequence to reference container / concatenate mapped segements ignor DELs
		for (int i = 0; i < record.getAlignmentBlocks().size(); i++) {
			if (referenceBases != null) {
				final int srcPos = record.getAlignmentBlocks().get(i).getReadStart() - 1;
				final int length = record.getAlignmentBlocks().get(i).getLength();
				System.arraycopy(
						record.getReadBases(), 
						srcPos, 
						referenceBases, 
						destPos, 
						length);
				destPos += length;
			}
		}

		// get MD string
		String MD = (String)o;
		// add potential missing number(s)
		MD = "0" + MD.toUpperCase();

		int position = 0;
		boolean nextInteger = true;
		// change to reference base based on MD string
//		int j = 0;
		for (String e : MD.split("((?<=[0-9]+)(?=[^0-9]+))|((?<=[^0-9]+)(?=[0-9]+))")) {
			if (nextInteger) { // match
				// use read sequence
				int matchLength = Integer.parseInt(e);
				position += matchLength;
				nextInteger = false;	
			} else if (e.charAt(0) == '^') {
				// ignore deletions from reference
				nextInteger = true;
			} else { // mismatch
//				try {
				referenceBases[position] = (byte)e.toCharArray()[0];
//				} catch (ArrayIndexOutOfBoundsException e2) {
//					String[] tmp = MD.split("((?<=[0-9]+)(?=[^0-9]+))|((?<=[^0-9]+)(?=[0-9]+))");
//					System.out.println(e2.toString());
//				}

				position += 1;
				nextInteger = true;
			}
//			++j;
		}
		// resize container if MD < read length
		if (position < referenceBases.length) {
			Arrays.copyOf(referenceBases, position);
		}

		return referenceBases;
	}
	
	protected void processRecord(SAMRecord record) {
		// init	
		int readPosition 	= 0;
		int genomicPosition = record.getAlignmentStart();

		int MDPosition = 0;
		byte[] referenceBases = null;

		// process record specific filters
		for (AbstractFilterStorage<?> filter : filterContainer.getPR()) {
			filter.processRecord(windowCoordinates.getGenomicWindowStart(), byte2int, record);
		}

		// process CIGAR -> SNP, INDELs
		final List<CigarElement> cigarElements = record.getCigar().getCigarElements();
		for (int i = 0; i < cigarElements.size(); ++i) {
			final CigarElement cigarElement = cigarElements.get(i);
			
			switch(cigarElement.getOperator()) {

			/*
			 * handle insertion
			 */
			case I:
				processInsertion(
						readPosition, 
						genomicPosition, 
						getAlignmentLength(i - 1, cigarElements), 
						getAlignmentLength(i + 1, cigarElements),  
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
				processAlignmentMatch(readPosition, genomicPosition, cigarElement, record, MDPosition, referenceBases);
				readPosition 	+= cigarElement.getLength();
				genomicPosition += cigarElement.getLength();
				MDPosition 		+= cigarElement.getLength();
				break;

			/*
			 * handle hard clipping 
			 */
			case H:
				processHardClipping(readPosition, genomicPosition, cigarElement, record);
				break;

			/*
			 * handle deletion from the reference and introns
			 */
			case D:
				processDeletion(
						readPosition, 
						genomicPosition, 
						getAlignmentLength(i - 1, cigarElements), 
						getAlignmentLength(i + 1, cigarElements),
						cigarElement, record);
				genomicPosition += cigarElement.getLength();
				break;

			case N:
				processSkipped(
						readPosition, 
						genomicPosition, 
						getAlignmentLength(i - 1, cigarElements), 
						getAlignmentLength(i + 1, cigarElements),
						cigarElement, record);
				genomicPosition += cigarElement.getLength();
				break;

			/*
			 * soft clipping
			 */
			case S:
				processSoftClipping(readPosition, genomicPosition, cigarElement, record);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * silent deletion from padded sequence
			 */
			case P:
				processPadding(
						readPosition, 
						genomicPosition,
						getAlignmentLength(i - 1, cigarElements), 
						getAlignmentLength(i + 1, cigarElements),
						cigarElement, 
						record);
				break;

			default:
				throw new RuntimeException("Unsupported Cigar Operator: " + cigarElement.getOperator().toString());
			}
		}
	}

	private int getAlignmentLength(final int i, final List<CigarElement> cigarElements) {
		if (i < 0 || i >= cigarElements.size()) {
			return 0;
		}
		
		final CigarElement cigarElement = cigarElements.get(i);
		switch (cigarElement.getOperator()) {
		case M:
		case EQ:
		case X:
			return cigarElement.getLength();

		default:
			return 0;
		}
	}
	
	protected void processAlignmentMatch(
			int readPosition, 
			int genomicPosition, 
			final CigarElement cigarElement, 
			final SAMRecord record,
			final int MDPosition, 
			byte[] referenceBases) {
		// process alignmentBlock specific filters

		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
			filter.processAlignmentBlock(readPosition, genomicPosition, cigarElement, record);
		}
		
		for (int offset = 0; offset < cigarElement.getLength(); ++offset) {
			final int baseI = byte2int[record.getReadBases()[readPosition + offset]];
			int qualI = record.getBaseQualities()[readPosition + offset];

			if (baseI == -1) {
				int windowPosition = windowCoordinates.convert2WindowPosition(genomicPosition + offset);
				int orientation = windowCoordinates.getOrientation(genomicPosition + offset);
				
				if (orientation == 0 && windowCache.getReferenceBase(windowPosition) == (byte)'N') {
					if (referenceBases == null) {
						referenceBases = parseMDField(record);
					}
					if (referenceBases.length > 0) {
						windowCache.addReferenceBase(windowPosition, referenceBases[MDPosition + offset]);
					}
				}

				continue;
			}

			// speedup: if orientation == 1 the remaining part of the read will be outside of the windowCache
			// ignore the overhanging part of the read until it overlaps with the window cache
			int windowPosition = windowCoordinates.convert2WindowPosition(genomicPosition + offset);
			int orientation = windowCoordinates.getOrientation(genomicPosition + offset);
			
			switch (orientation) {
			case 1:
				if ((genomicPosition + offset) - windowCoordinates.getGenomicWindowEnd() <= distance) {
					if (qualI >= sampleParameters.getMinBASQ()) {
						for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
							filter.processAlignmentMatch(windowPosition, readPosition + offset, genomicPosition + offset, cigarElement, record, baseI, qualI);
						}
					}
				} else {
					return;
				}
				break;
			case -1: // speedup jump to covered position
				if (windowCoordinates.getGenomicWindowStart() - (genomicPosition + offset) > distance) {
					offset += windowCoordinates.getGenomicWindowStart() - (genomicPosition + offset) - distance - 1;
				} else {
					if (qualI >= sampleParameters.getMinBASQ()) {
						for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
							filter.processAlignmentMatch(windowPosition, readPosition + offset, genomicPosition + offset, cigarElement, record, baseI, qualI);
						}
					}
				}
				break;
			case 0:
				if (windowPosition >= 0) {
					if (qualI >= sampleParameters.getMinBASQ()) {
						addHighQualityBaseCall(windowPosition, baseI, qualI, strand);

						// process any alignmentMatch specific filters
						for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.M)) {
							filter.processAlignmentMatch(windowPosition, readPosition + offset, genomicPosition + offset, cigarElement, record, baseI, qualI);
						}
					} else if (parameters.collectLowQualityBaseCalls()) { 
						addLowQualityBaseCall(windowPosition, baseI, qualI, strand);
					}
					if (windowCache.getReferenceBase(windowPosition) == (byte)'N') {
						if (referenceBases == null) {
							referenceBases = parseMDField(record);
						}
						if (referenceBases.length > 0) {
							windowCache.addReferenceBase(windowPosition, referenceBases[MDPosition + offset]);
						}
					}
				}
				break;
			}
		}
	}

	protected void processInsertion(
			int readPosition, 
			int genomicPosition,
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.I)) {
			filter.processInsertion(
					readPosition, 
					genomicPosition, 
					upstreamMatch, 
					downstreamMatch, 
					cigarElement, 
					byte2int, record);
		}
	}

	protected void processDeletion(
			int readPosition, 
			int genomicPosition, 
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.D)) {
			filter.processDeletion(
					readPosition, 
					genomicPosition, 
					upstreamMatch,
					downstreamMatch,
					cigarElement, 
					byte2int, record);
		}
	}

	protected void processSkipped(
			int readPosition, 
			int genomicPosition,
			int upstreamMatch,
			int downstreamMatch,
			final CigarElement cigarElement, 
			final SAMRecord record) {
		for (AbstractFilterStorage<?> filter : filterContainer.get(CigarOperator.N)) {
			filter.processSkipped(
					readPosition, 
					genomicPosition,
					upstreamMatch,
					downstreamMatch,
					cigarElement, 
					byte2int, record);
		}
	}

}