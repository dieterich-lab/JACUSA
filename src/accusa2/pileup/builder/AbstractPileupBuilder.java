package accusa2.pileup.builder;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMValidationError;
import accusa2.cache.Coordinate;
import accusa2.cli.Parameters;
import accusa2.filter.process.AbstractPileupBuilderFilter;
import accusa2.filter.samtag.SamTagFilter;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.AnnotatedCoordinate;

public abstract class AbstractPileupBuilder {

	// in genomic coordinates
	protected String contig;
	protected int genomicWindowStart;
	protected int windowSize;

	protected int currentGenomicPosition;
	protected int maxGenomicPosition;

	protected SAMRecord[] SAMRecordsBuffer;
	protected SAMRecord currentRecord;
	protected int[] currentBases;
	protected byte[] currentQuals;

	protected int[] coverageCache;
	protected int[][] baseCache;
	protected int[][][] qualCache;

	protected int[][] filteredCoverageCache;
	protected int[][][] filteredBaseCache;
	protected int[][][][] filteredQualCache;

	protected SAMFileReader reader;

	protected int filteredSAMRecords;
	
	protected Parameters parameters;
	
	protected List<Coordinate> indelsBuffer;
	protected List<Coordinate> skippedBuffer;

	protected List<AbstractPileupBuilderFilter> pileupBuilderFilters;

	public abstract boolean hasNext();
	public abstract Pileup next();

	public AbstractPileupBuilder(AnnotatedCoordinate annotatedCoordinate, SAMFileReader SAMFileReader, Parameters parameters) {
		contig				= annotatedCoordinate.getSequenceName();
		genomicWindowStart 	= Integer.MAX_VALUE; //annotatedCoordinate.getStart();
		currentGenomicPosition 	= annotatedCoordinate.getStart();
		windowSize 			= parameters.getWindowSize();
		maxGenomicPosition 	= Math.min(annotatedCoordinate.getEnd(), SAMFileReader.getFileHeader().getSequence(contig).getSequenceLength());

		this.parameters		= parameters;
		pileupBuilderFilters= parameters.getPileupBuilderFilters().getPileupBuilderFilters();

		// build cache
		currentRecord 		= null;
		SAMRecordsBuffer	= new SAMRecord[10000];
		init();

		this.reader			= SAMFileReader;

		filteredSAMRecords	= 0;

		// init
		currentBases 		= new int[windowSize];
		currentQuals 		= new byte[windowSize];
		indelsBuffer 		= new ArrayList<Coordinate>(3);
		skippedBuffer 		= new ArrayList<Coordinate>(3);
		adjustCurrentGenomicPosition(currentGenomicPosition);
	}

	protected void init() {
		coverageCache = new int[windowSize];
		baseCache = new int[windowSize][Pileup.BASES2.length];
		qualCache = new int[windowSize][Pileup.BASES2.length][Phred2Prob.MAX_Q];

		if(pileupBuilderFilters.size() > 0) {
			filteredCoverageCache = new int[windowSize][pileupBuilderFilters.size()];
			filteredBaseCache = new int[windowSize][pileupBuilderFilters.size()][Pileup.BASES2.length];
			filteredQualCache = new int[windowSize][pileupBuilderFilters.size()][Pileup.BASES2.length][Phred2Prob.MAX_Q];
		}
	}

	/**
	 * 
	 * @param targetPosition
	 * @return
	 */
	protected int getNextValidPosition(int targetPosition) {
		SAMRecordIterator iterator = reader.query(contig, targetPosition, maxGenomicPosition, false);
		while(iterator.hasNext() ) {
			SAMRecord record = iterator.next();

			if(isValid(record)) {
				iterator.close();
				iterator = null;
				return record.getAlignmentStart();
			}
		}
		iterator.close();
		iterator = null;

		// if no more reads are found 
		return -1;
	}

	/**
	 * Reset all pileup in window
	 */
	protected void clearPileupCache() {
		Arrays.fill(coverageCache, 0);
		for(int f = 0; f < pileupBuilderFilters.size(); ++f) {
			Arrays.fill(filteredCoverageCache[f], 0);
		}

		for(int i = 0; i < windowSize; ++i) {
			Arrays.fill(baseCache[i], 0);
			for(int f = 0; f < pileupBuilderFilters.size(); ++f) {
				Arrays.fill(filteredBaseCache[i][f], 0);
				for(int b = 0; b < Pileup.BASES2.length; ++b) {
					Arrays.fill(filteredQualCache[i][f][b], 0);
				}
			}

			for(int b = 0; b < Pileup.BASES2.length; ++b) {
				Arrays.fill(qualCache[i][b], 0);
			}
		}
	}

	/**
	 * 
	 * @param targetPosition
	 * @return
	 */
	public boolean adjustCurrentGenomicPosition(int targetPosition) {
		currentGenomicPosition = targetPosition;
		if(isContainedInWindow(targetPosition)) {
			return true;
		}
		genomicWindowStart = targetPosition;
		clearPileupCache();

		// fill window
		SAMRecordIterator iterator = reader.query(contig, genomicWindowStart, Math.min(getWindowEnd(), maxGenomicPosition), false);

		boolean windowHit = false;
		int count = 0;
		while(iterator.hasNext()) {
			SAMRecord record = iterator.next();

			if(isValid(record)) {
				SAMRecordsBuffer[count++] = record;
			} else {
				filteredSAMRecords++;
			}

			// process buffer
			if(count >= SAMRecordsBuffer.length) {
				for(int i = 0; i < count; ++i) {
					try {
						processRecord(SAMRecordsBuffer[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				count = 0;
				windowHit = true;
			}
		}
		iterator.close();

		if(!windowHit && count == 0) {
			int nextPosition = getNextValidPosition(targetPosition + windowSize);
			if(nextPosition > 0) {
				return adjustCurrentGenomicPosition(nextPosition);
			}
			return false;
		} else {
			for(int i = 0; i < count; ++i) {
				try {
					processRecord(SAMRecordsBuffer[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return true;
		}
	}

	public boolean isContainedInGenome(int genomicPosition) {
		return genomicPosition <= maxGenomicPosition && genomicPosition > 0;
	}

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

		// TODO check behavior of flags
		if(!samRecord.getReadUnmappedFlag()
				&& !samRecord.getNotPrimaryAlignmentFlag()
				&& (mapq < 0 || mapq >= parameters.getMinMAPQ())
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
	 * Calculates genomicPosition or -1 if genomicPosition is outside the window 
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

	public int getFilteredSAMRecords() {
		return filteredSAMRecords;
	}

	/**
	 * 
	 * @param record
	 * @throws Exception
	 */
	protected void processRecord(final SAMRecord record) throws Exception {
		currentRecord = record;

		int readPosition 	= 0;
		int genomicPosition = currentRecord.getAlignmentStart();

		Arrays.fill(currentBases, -1);
		Arrays.fill(currentQuals, (byte)-1);

		indelsBuffer.clear();
		skippedBuffer.clear();

		for(final CigarElement cigarElement : currentRecord.getCigar().getCigarElements()) {

			switch(cigarElement.getOperator()) {

			/*
			 * handle insertion
			 */
			case I:
				processInsertion(readPosition, genomicPosition, cigarElement, currentRecord);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * handle alignment/sequence match and mismatch
			 */
			case M:
			case EQ:
			case X:
				processAlignmetMatch(readPosition, genomicPosition, cigarElement, currentRecord);
				readPosition += cigarElement.getLength();
				genomicPosition += cigarElement.getLength();
				break;

			/*
			 * handle hard clipping 
			 */
			case H:
				processHardClipping(readPosition, genomicPosition, cigarElement, currentRecord);
				break;

			/*
			 * handle deletion from the reference and introns
			 */
			case D:
				processDeletion(readPosition, genomicPosition, cigarElement, currentRecord);
				genomicPosition += cigarElement.getLength();
				break;

			case N:
				processSkipped(readPosition, genomicPosition, cigarElement, currentRecord);
				genomicPosition += cigarElement.getLength();
				break;

			/*
			 * soft clipping
			 */
			case S:
				processSoftClipping(readPosition, genomicPosition, cigarElement, currentRecord);
				readPosition += cigarElement.getLength();
				break;

			/*
			 * silent deletion from padded sequence
			 */
			case P:
				processPadding(readPosition, genomicPosition, cigarElement, currentRecord);
				break;

			default:
				throw new RuntimeException("Unsupported Cigar Operator: " + cigarElement.getOperator().toString());
			}
		}

		// filter
		for(AbstractPileupBuilderFilter pileupBuilderFilter : pileupBuilderFilters) {
			if(pileupBuilderFilter != null) {
				pileupBuilderFilter.process(this);
			}
		}
	}

	public int[] getCoverageCache() {
		return coverageCache;
	}
	
	public int[][] getBaseCache() {
		return baseCache;
	}
	
	public int[][][] getQualCache() {
		return qualCache;
	}

	public int[][] getFilteredCoverageCache() {
		return filteredCoverageCache;
	}
	
	public int[][][] getFilteredBaseCache() {
		return filteredBaseCache;
	}
	
	public int[][][][] getFilteredQualCache() {
		return filteredQualCache;
	}

	public SAMRecord getCurrentRecord() {
		return currentRecord;
	}

	public int[] getCurrentBases() {
		return currentBases;
	}

	public byte[] getCurrentQuals() {
		return currentQuals;
	}

	public List<Coordinate> getIndels() {
		return indelsBuffer;
	}
	
	public List<Coordinate> getSkipped() {
		return skippedBuffer;
	}

	public Parameters getParameters() {
		return parameters;
	}

	public int getGenomicWindowStart() {
		return genomicWindowStart;
	}
	
	abstract protected void processInsertion(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record);
	abstract protected void processAlignmetMatch(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record);	
	abstract protected void processHardClipping(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record);
	abstract protected void processDeletion(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record);
	abstract protected void processSkipped(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record);
	abstract protected void processSoftClipping(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record);
	abstract protected void processPadding(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record);

}
