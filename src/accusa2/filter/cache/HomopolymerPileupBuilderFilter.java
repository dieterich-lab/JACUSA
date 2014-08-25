package accusa2.filter.cache;

import java.util.Arrays;

import accusa2.pileup.builder.AbstractPileupBuilder;

public class HomopolymerPileupBuilderFilter extends AbstractPileupBuilderFilterCache {

	private int length;
	private int minDistance;

	/**
	 * 
	 * @param c
	 * @param distance
	 */
	public HomopolymerPileupBuilderFilter(char c, int length, int distance) {
		super(c);
		this.length = length;
		this.minDistance = distance;
	}

	/**
	 * FIXME very very complicated
	 */
	@Override
	public void process(final AbstractPileupBuilder pileupBuilder) {
		final int[] bases = new int[pileupBuilder.getCurrentBases().length];
		System.arraycopy(pileupBuilder.getCurrentBases(), 0, bases, 0, pileupBuilder.getCurrentBases().length);

		final byte[] quals = new byte[pileupBuilder.getCurrentQuals().length];
		System.arraycopy(pileupBuilder.getCurrentQuals(), 0, quals, 0, pileupBuilder.getCurrentQuals().length);

		FilterConfig pileupFilter = pileupBuilder.getParameters().getFilterConfig();

		int[] count = new int[pileupBuilder.getCurrentRecord().getReadLength()];
		int[] index = new int[pileupBuilder.getCurrentRecord().getReadLength()];
		Arrays.fill(index, -1);

		int i = 0;
		int j = 0;
		index[i] = j;
		for(i = 1; i < index.length; ++i) {
			if(pileupBuilder.getCurrentRecord().getReadBases()[i - 1] != pileupBuilder.getCurrentRecord().getReadBases()[i]) {
				++j;
			}
			index[i] = j;
			++count[j];
		}

		for(i = 0; i < pileupBuilder.getCurrentRecord().getReadLength(); ++i) {
			if(count[index[i]] >= length) {
				process(pileupBuilder.getCurrentRecord().getAlignmentStart() + i, bases, quals, pileupBuilder, pileupFilter);
			}
		}
	}

	private void process(int genomicPosition, int[] bases, byte[] quals, AbstractPileupBuilder pileupBuilder, FilterConfig pileupFilter) {
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
	
	public int getLength() {
		return length;
	}

	/**
	 * 
	 * @return
	 */
	public int getDistance() {
		return minDistance;
	}

}
