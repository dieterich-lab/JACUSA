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
import net.sf.samtools.SAMRecord;

/**
 * @author michael
 *
 */
public class UndirectedPileupBuilder extends AbstractPileupBuilder {

	protected WindowCache windowCache;
	protected FilterContainer filterContainer;

	protected STRAND strand;
	protected int windowPosition;

	public UndirectedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);

		windowCache 	= new WindowCache(windowSize, baseConfig.getBases().length);
		filterContainer	= parameters.getFilterConfig().createFilterContainer();
		
		strand 			= STRAND.UNKNOWN;
	}

	public FilterContainer getFilterContainer(int windowPosition, STRAND strand) {
		return filterContainer;
	}

	public Pileup getPileup(int windowPosition, STRAND strand) {
		Pileup pileup = new DefaultPileup(contig, getGenomicPosition(windowPosition), strand, baseConfig.getBases().length);

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(windowCache.baseCount[windowPosition]);
		pileup.getCounts().setQualCount(windowCache.qualCount[windowPosition]);

		if (STRAND.REVERSE == strand) {
			pileup = pileup.complement();
		}

		return pileup;
	}

	@Override
	public void clearCache() {
		windowCache.clear();
		filterContainer.clear();
		filterContainer.setGenomicWindowStart(genomicWindowStart);
	}
	
	@Override
	protected void add2WindowCache(int windowPosition, byte base, byte qual, SAMRecord record) {
		int baseI = baseConfig.getBaseI(base);
		// ignore bases that should not be considered - see processAlignmentBlock
		if (baseI < 0) {
			return;
		}
		
		windowCache.add(windowPosition, baseI, qual);
	}
	
	@Override
	protected void processFilters(SAMRecord record) {
		// let the filter decide what data they need
		filterContainer.processRecord(record);
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		return getCoverage(windowPosition, STRAND.UNKNOWN) >= sample.getMinCoverage();
	}

	@Override
	public int getCoverage(int windowPosition, STRAND strand) {
		return windowCache.getCoverage(windowPosition);
	}
	
}