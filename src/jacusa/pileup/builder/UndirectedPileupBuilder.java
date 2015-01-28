/**
 * 
 */
package jacusa.pileup.builder;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;

/**
 * @author michael
 *
 */
public class UndirectedPileupBuilder extends AbstractPileupBuilder {
	
	public UndirectedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, STRAND.UNKNOWN, reader, sample, parameters);

	}

	public FilterContainer getFilterContainer(int windowPosition, STRAND strand) {
		return filterContainer;
	}

	public Pileup getPileup(int windowPosition, STRAND strand) {
		Pileup pileup = new DefaultPileup(
				windowCoordinates.getContig(), 
				windowCoordinates.getGenomicPosition(windowPosition), 
				strand, baseConfig.getBaseLength());

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(windowCache.getBaseCount(windowPosition));
		pileup.getCounts().setQualCount(windowCache.getQualCount(windowPosition));

		if (STRAND.REVERSE == strand) {
			pileup = pileup.complement();
		}

		return pileup;
	}

	@Override
	public void clearCache() {
		windowCache.clear();
		filterContainer.clear();
	}
	
	@Override
	protected void add2WindowCache(int windowPosition, int baseI, int qual, STRAND strand) {
		windowCache.add(windowPosition, baseI, qual);
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		return getCoverage(windowPosition, STRAND.UNKNOWN) >= sampleParameters.getMinCoverage();
	}

	@Override
	public int getCoverage(int windowPosition, STRAND strand) {
		return windowCache.getCoverage(windowPosition);
	}

}