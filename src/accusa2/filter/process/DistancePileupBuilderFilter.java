package accusa2.filter.process;

import accusa2.cache.Coordinate;
import accusa2.pileup.builder.AbstractPileupBuilder;

public class DistancePileupBuilderFilter extends AbstractPileupBuilderFilter {

	private int distance;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public DistancePileupBuilderFilter(char c, int distance) {
		super(c);
		this.distance = distance;
	}

	/**
	 * 
	 */
	@Override
	public void process(final AbstractPileupBuilder pileupBuilder) {
		final int[] bases = new int[pileupBuilder.getCurrentBases().length];
		System.arraycopy(pileupBuilder.getCurrentBases(), 0, bases, 0, pileupBuilder.getCurrentBases().length);

		final byte[] quals = new byte[pileupBuilder.getCurrentQuals().length];
		System.arraycopy(pileupBuilder.getCurrentQuals(), 0, quals, 0, pileupBuilder.getCurrentQuals().length);

		PileupBuilderFilter pileupFilter = pileupBuilder.getParameters().getPileupBuilderFilters();

		// process first and last base of read
		for(int i = 0; i < distance; ++i) {
			process(pileupBuilder.getCurrentRecord().getAlignmentStart() + i, bases, quals, pileupBuilder, pileupFilter);
			process(pileupBuilder.getCurrentRecord().getAlignmentEnd() - i, bases, quals, pileupBuilder, pileupFilter);
		}

		// process INDELs
		for(final Coordinate indel : pileupBuilder.getIndels()) {
			for(int i = 0; i < distance; ++i) {
				process(indel.getGenomicPosition() - i, bases, quals, pileupBuilder, pileupFilter);
				process(indel.getGenomicPosition() + indel.getCigarElement().getLength() + i, bases, quals, pileupBuilder, pileupFilter);
			}
		}

		// process Skipped regions
		for(final Coordinate skipped : pileupBuilder.getSkipped()) {
			for(int i = 0; i < distance; ++i) {
				process(skipped.getGenomicPosition() - i, bases, quals, pileupBuilder, pileupFilter);
				process(skipped.getGenomicPosition() + skipped.getCigarElement().getLength() + i, bases, quals, pileupBuilder, pileupFilter);
			}
		}
	}

	/**
	 * 
	 * @param windowPosition
	 * @param bases
	 * @param quals
	 * @param pileupCache
	 * @param pileupFilter
	 */
	private void process(int genomicPosition, int[] bases, byte[] quals, AbstractPileupBuilder pileupBuilder, PileupBuilderFilter pileupFilter) {
		// outside of cache or already filtered (bases[windowPosition] < 0)

		// ugly hack depending if stranded or not stranded windowsPosition has different meanings
		if(!pileupBuilder.isContainedInWindow(genomicPosition) || bases[genomicPosition - pileupBuilder.getGenomicWindowStart()] < 0) {
			return;
		}

		int windowPosition = pileupBuilder.convertGenomicPosition2WindowPosition(genomicPosition);
		int f = pileupFilter.getI(getC());
		// UGLY CODE genomicPosition - pileupBuilder.getGenomicWindowStart() != genomicPosition - pileupBuilder.getGenomicWindowStart() in case of stranded pileups
		pileupBuilder.getFilteredCoverageCache()[windowPosition][f]++;
		pileupBuilder.getFilteredBaseCache()[windowPosition][f][bases[genomicPosition - pileupBuilder.getGenomicWindowStart()]]++;
		pileupBuilder.getFilteredQualCache()[windowPosition][f][bases[genomicPosition - pileupBuilder.getGenomicWindowStart()]][quals[genomicPosition - pileupBuilder.getGenomicWindowStart()]]++;

		// reset mark visited positions
		bases[genomicPosition - pileupBuilder.getGenomicWindowStart()] = -1;
		quals[genomicPosition - pileupBuilder.getGenomicWindowStart()] = -1;		
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return distance;
	}
	
}
