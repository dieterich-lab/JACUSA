package accusa2.pileup.builder;

import java.util.Arrays;
import java.util.List;

import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import accusa2.cache.Coordinate;
import accusa2.cli.Parameters;
import accusa2.pileup.Pileup.STRAND;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
public class DirectedPileupBuilder extends UndirectedPileupBuilder {

	protected SAMRecord[] reverseSAMRecordsBuffer;
	private STRAND strand;

	public DirectedPileupBuilder(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader reader, final Parameters parameters) {
		super(annotatedCoordinate, reader, parameters);
	}

	/**
	 * +-+-...+-+-
	 */
	@Override
	protected void init() {
		int size = 2 * windowSize;
		coverageCache = new int[size];
		baseCache = new int[size][Pileup.BASES2.length];
		qualCache = new int[size][Pileup.BASES2.length][Phred2Prob.MAX_Q];

		if(pileupBuilderFilters.size() > 0) {
			filteredCoverageCache = new int[size][pileupBuilderFilters.size()];
			filteredBaseCache = new int[size][pileupBuilderFilters.size()][Pileup.BASES2.length];
			filteredQualCache = new int[size][pileupBuilderFilters.size()][Pileup.BASES2.length][Phred2Prob.MAX_Q];
		}

		reverseSAMRecordsBuffer = new SAMRecord[SAMRecordsBuffer.length];
		// always consider forward strand first
		strand = STRAND.FORWARD;
	}

	protected void clearPileupCache() {
		int size = 2 * windowSize;
		
		Arrays.fill(coverageCache, 0);
		for(int f = 0; f < pileupBuilderFilters.size(); ++f) {
			Arrays.fill(filteredCoverageCache[f], 0);
		}

		for(int i = 0; i < size; ++i) {
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

		// always consider forward strand first
		strand = STRAND.FORWARD;
	}

	@Override
	public boolean hasNext() {
		// ensure that we stay in the range of contig
		while(isContainedInGenome(currentGenomicPosition)) {
			if(isContainedInWindow(currentGenomicPosition)) {
				if(isValid(currentGenomicPosition)) {
					return true;
				} else {
					switch(strand) {
					case FORWARD:
						strand 	= STRAND.REVERSE;
						break;
					case REVERSE:
						++currentGenomicPosition;
						strand 	= STRAND.FORWARD;
						break;
					case UNKNOWN:
						strand 	= STRAND.FORWARD;
						break;
					}
				}
			} else if(!adjustCurrentGenomicPosition(currentGenomicPosition)) {
				break;
			}
		}

		return false;
	}

	// FIXME confusing code
	@Override
	public int convertGenomicPosition2WindowPosition(int genomicPosition) {
 		if(genomicPosition < genomicWindowStart) {
			return -2;
		} else if(genomicPosition > getWindowEnd()){
			return -1;
		}

 		int pos = 2 * (genomicPosition - genomicWindowStart);
 		if(strand == STRAND.REVERSE) {
 			pos++;
 		}
		return pos;
	}

	@Override
	public Pileup next() {
		final Pileup pileup = super.next();
		pileup.setStrand(strand);

		switch(strand) {

		case FORWARD:
			strand 	= STRAND.REVERSE;
			// stay at the same position but switch the strand
			--currentGenomicPosition;
			break;

		case REVERSE:
			strand 	= STRAND.FORWARD;
			break;

		case UNKNOWN:
			break;
		}

		return pileup;
	}

	@Override
	public boolean adjustCurrentGenomicPosition(int potentialTargetPosition) {
		currentGenomicPosition = potentialTargetPosition;
		if(isContainedInWindow(potentialTargetPosition)) {
			// always start with the forward strand
			strand = STRAND.FORWARD;
			return true;
		}
		genomicWindowStart = potentialTargetPosition;
		clearPileupCache();

		// fill window
		SAMRecordIterator iterator = reader.query(contig, genomicWindowStart, Math.min(getWindowEnd(), maxGenomicPosition), false);

		boolean forwardWindowHit = false;
		int forwardCount = 0;

		boolean reverseWindowHit = false;
		int reverseCount = 0;

		while(iterator.hasNext()) {
			SAMRecord record = iterator.next();

			if(isValid(record)) {
				if(record.getReadNegativeStrandFlag()) {
					reverseSAMRecordsBuffer[reverseCount++] = record;
				} else {
					SAMRecordsBuffer[forwardCount++] = record;
				}
			} else {
				filteredSAMRecords++;
			}

			// process buffer for forward reads
			if(forwardCount >= SAMRecordsBuffer.length) {
				strand 	= STRAND.FORWARD;
				for(int i = 0; i < forwardCount; ++i) {
					try {
						processRecord(SAMRecordsBuffer[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				forwardCount = 0;
				forwardWindowHit = true;
			}
			
			// process buffer for reverse reads
			if(reverseCount >= reverseSAMRecordsBuffer.length) {
				strand 	= STRAND.REVERSE;
				for(int i = 0; i < reverseCount; ++i) {
					try {
						processRecord(reverseSAMRecordsBuffer[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				reverseCount = 0;
				reverseWindowHit = true;
			}
		}
		iterator.close();

		// if no valid SAMRecord could be processed within current window
		if(!forwardWindowHit && !reverseWindowHit && forwardCount == 0 && reverseCount == 0) {
			int nextPosition = getNextValidPosition(potentialTargetPosition + windowSize);
			if(nextPosition > 0) {
				strand = STRAND.FORWARD;
				return adjustCurrentGenomicPosition(nextPosition);
			}
			strand = STRAND.FORWARD;
			return false;
		} else {
			strand 	= STRAND.FORWARD;
			for(int i = 0; i < forwardCount; ++i) {
				try {
					processRecord(SAMRecordsBuffer[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			strand 	= STRAND.REVERSE;
			for(int i = 0; i < reverseCount; ++i) {
				try {
					processRecord(reverseSAMRecordsBuffer[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			strand = STRAND.FORWARD;
			return true;
		}
	}

	@Override
	protected int cachePosition(final int readPosition, final int genomicPosition, final CigarElement cigarElement, final List<Coordinate> indels, final List<Coordinate> skipped, final SAMRecord record) {
		final int windowPosition = convertGenomicPosition2WindowPosition(genomicPosition);

		if(windowPosition >= 0 && (parameters.getMaxDepth() == -1 || coverageCache[windowPosition] <= parameters.getMaxDepth()) ) {
			int base = Pileup.BASE2INT.get((char)record.getReadBases()[readPosition]);
			if(strand == STRAND.REVERSE) {
				base = Pileup.COMPLEMENT[base];
			}
			final byte qual = record.getBaseQualities()[readPosition];

			// DO NOT CHANGE - windowPosition != genomicPosition - genomicWindowStart
			currentBases[genomicPosition - genomicWindowStart] = base;
			currentQuals[genomicPosition - genomicWindowStart] = qual;

			coverageCache[windowPosition]++;
			baseCache[windowPosition][base]++;
			qualCache[windowPosition][base][qual]++;
		}

		return windowPosition;
	}

}
