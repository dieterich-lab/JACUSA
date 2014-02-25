/**
 * 
 */
package accusa2.pileup.builder;

import java.util.List;


import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cache.Coordinate;
import accusa2.cli.Parameters;
import accusa2.pileup.Pileup;
import accusa2.pileup.Pileup.STRAND;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
public class UndirectedPileupBuilder extends AbstractPileupBuilder {

	public UndirectedPileupBuilder(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader reader, final Parameters parameters) {
		super(annotatedCoordinate, reader, parameters);
	}

	@Override
	public boolean hasNext() {
		// ensure that we stay in the range of contig
		while(isContainedInGenome(currentGenomicPosition)) {
			if(isContainedInWindow(currentGenomicPosition)) {
				if(isValid(currentGenomicPosition)) {
					return true;
				} else {
					++currentGenomicPosition;
				}
				
			} else if(!adjustCurrentGenomicPosition(currentGenomicPosition)) {
				break;
			}
		}

		return false;
	}

	@Override
	public Pileup next() {
		final int windowPosition = convertGenomicPosition2WindowPosition(currentGenomicPosition);
		final Pileup pileup = new Pileup(contig, currentGenomicPosition, STRAND.UNKNOWN);

		// copy base and qual info from cache
		pileup.setBaseCount(new int[Pileup.BASES2.length]);
		pileup.setQualCount(new int[Pileup.BASES2.length][Phred2Prob.MAX_Q]);
		System.arraycopy(baseCache[windowPosition], 0, pileup.getBaseCount(), 0, baseCache[windowPosition].length);
		System.arraycopy(qualCache[windowPosition], 0, pileup.getQualCount(), 0, qualCache[windowPosition].length);

		// copy filter information to pileup
		if(pileupBuilderFilters.size() > 0) {
			Pileup[] filteredPileups = new Pileup[pileupBuilderFilters.size()];
			for(int f = 0; f < pileupBuilderFilters.size(); ++f) {
				Pileup filteredPileup = new Pileup();
	
				int[] baseCount = new int[filteredBaseCache[windowPosition][f].length];
				int[][] qualCount = new int[Pileup.BASES2.length][Phred2Prob.MAX_Q];
				filteredPileup.setBaseCount(baseCount);
				filteredPileup.setQualCount(qualCount);
	
				System.arraycopy(filteredBaseCache[windowPosition][f], 0, filteredPileup.getBaseCount(), 0, Pileup.BASES2.length);
				System.arraycopy(filteredQualCache[windowPosition][f], 0, filteredPileup.getQualCount(), 0, filteredQualCache[windowPosition][f].length);

				filteredPileups[f] = filteredPileup;
			}
			pileup.setFilteredPileups(filteredPileups);
		}

		++currentGenomicPosition;
		return pileup;
	}

	protected boolean isValid(int genomicPosition) {
		int windowPosition = convertGenomicPosition2WindowPosition(genomicPosition);
		return coverageCache[windowPosition] >= parameters.getMinCoverage();
	}

	@Override
	protected void processInsertion(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		indelsBuffer.add(new Coordinate(genomicPosition, readPosition, cigarElement));
	}

	@Override
	protected void processAlignmetMatch(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		for(int i = 0; i < cigarElement.getLength(); ++i) {
			if(record.getBaseQualities()[readPosition] >= parameters.getMinBASQ() && record.getReadBases()[readPosition] != 'N') {
				int windowPosition = cachePosition(readPosition, genomicPosition, cigarElement, indelsBuffer, skippedBuffer, record);
				if(windowPosition == -1) {
					return;
				}
			}

			// iterate
			++readPosition;
			++genomicPosition;
		}
	}

	@Override
	protected void processHardClipping(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		System.err.println("Hard Clipping not handled yet!");
	}

	@Override
	protected void processDeletion(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		processInsertion(readPosition, genomicPosition, cigarElement, record);
	}

	@Override
	protected void processSkipped(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		skippedBuffer.add(new Coordinate(genomicPosition, readPosition, cigarElement));
	}

	@Override
	protected void processSoftClipping(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) { 
		// ignore
	}

	@Override
	protected void processPadding(int readPosition, int genomicPosition, final CigarElement cigarElement, final SAMRecord record) {
		System.err.println("Padding not handled yet!");
	}

	/**
	 * 
	 * @param readPosition
	 * @param genomicPosition
	 * @param cigarElement
	 * @param record
	 * @return
	 */
	protected int cachePosition(final int readPosition, final int genomicPosition, final CigarElement cigarElement, final List<Coordinate> indels, final List<Coordinate> skipped, final SAMRecord record) {
		final int windowPosition = convertGenomicPosition2WindowPosition(genomicPosition);

		if(windowPosition >= 0 && (parameters.getMaxDepth() == -1 || coverageCache[windowPosition] <= parameters.getMaxDepth()) ) {
			final int base = Pileup.BASE2INT.get((char)record.getReadBases()[readPosition]);
			final byte qual = record.getBaseQualities()[readPosition];

			currentBases[genomicPosition - genomicWindowStart] = base;
			currentQuals[genomicPosition - genomicWindowStart] = qual;

			coverageCache[windowPosition]++;
			baseCache[windowPosition][base]++;
			qualCache[windowPosition][base][qual]++;
		}

		return windowPosition;
	}

}
