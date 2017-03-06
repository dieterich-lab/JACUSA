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
 * @author Michael Piechotta
 *
 */
public class UnstrandedPileupBuilder extends AbstractPileupBuilder {
	
	public UnstrandedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, STRAND.UNKNOWN, reader, sample, parameters);
	}

	public FilterContainer getFilterContainer(int windowPosition, STRAND strand) {
		return filterContainer;
	}

	@Override
	public Pileup getPileup(int windowPosition, STRAND strand) {
		Pileup pileup = new DefaultPileup(
				windowCoordinates.getContig(), 
				windowCoordinates.getGenomicPosition(windowPosition), 
				strand, baseConfig.getBaseLength());

		// set base and qual info from cache
		pileup.setCounts(windowCache.getCounts(windowPosition));

		byte refBaseByte = windowCache.getReferenceBase(windowPosition);
		if (refBaseByte != (byte)'N') {
			pileup.setRefBase((char)refBaseByte);
		}

		// and complement if needed
		if (strand == STRAND.REVERSE) {
			pileup = pileup.invertBaseCount();
		}

		return pileup;
	}

	@Override
	public void clearCache() {
		windowCache.clear();

		filterContainer.clear();
	}

	@Override
	protected void addHighQualityBaseCall(int windowPosition, int baseI, int qualI, STRAND strand) {
		windowCache.addHighQualityBaseCall(windowPosition, baseI, qualI);
	}
	
	@Override
	protected void addLowQualityBaseCall(int windowPosition, int baseI, int qualI, STRAND strand) {
		windowCache.addLowQualityBaseCall(windowPosition, baseI, qualI);
	}

	@Override
	public WindowCache getWindowCache(STRAND strand) {
		return windowCache;
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		// for unstrandedPileup we ignore strand
		return getCoverage(windowPosition, STRAND.UNKNOWN) >= sampleParameters.getMinCoverage();
	}

	@Override
	public int getCoverage(int windowPosition, STRAND strand) {
		// for unstrandedPileup we ignore strand
		return windowCache.getCoverage(windowPosition);
	}

}