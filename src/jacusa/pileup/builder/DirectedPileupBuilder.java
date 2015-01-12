package jacusa.pileup.builder;


import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.process.phred2prob.Phred2Prob;
import jacusa.util.Coordinate;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;

/**
 * @author michael
 *
 */
public class DirectedPileupBuilder extends AbstractPileupBuilder {

	protected WindowCache forwardWindowCache;
	protected WindowCache reverseWindowCache;

	protected FilterContainer forwardFilterContainer;
	protected FilterContainer reverseFilterContainer;

	protected int windowPosition;

	public DirectedPileupBuilder(
			final Coordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);

		forwardWindowCache = new WindowCache(windowSize, baseConfig.getBases().length);
		reverseWindowCache = new WindowCache(windowSize, baseConfig.getBases().length);

		forwardFilterContainer = parameters.getFilterConfig().createFilterContainer();
		reverseFilterContainer = parameters.getFilterConfig().createFilterContainer();
	}

	@Override
	public void clearCache() {
		forwardWindowCache.clear();
		reverseWindowCache.clear();

		forwardFilterContainer.clear();
		forwardFilterContainer.setGenomicWindowStart(genomicWindowStart);
		
		reverseFilterContainer.clear();
		reverseFilterContainer.setGenomicWindowStart(genomicWindowStart);
	}

	@Override
	public int getCoverage(int windowPosition, STRAND strand) {
		switch (strand) {
		case FORWARD:
			return forwardWindowCache.getCoverage(windowPosition);

		case REVERSE:
			return reverseWindowCache.getCoverage(windowPosition);

		case UNKNOWN:
		default:
			return forwardWindowCache.getCoverage(windowPosition) + reverseWindowCache.getCoverage(windowPosition);
		}
	}

	@Override
	public FilterContainer getFilterContainer(int windowPosition, STRAND strand) {
		switch (strand) {
		case FORWARD:
			return forwardFilterContainer;

		case REVERSE:
			return reverseFilterContainer;
			
		case UNKNOWN:
		default:
			return null;
		} 
	}
	
	@Override
	public Pileup getPileup(int windowPosition, STRAND strand) {
		final Pileup pileup = new DefaultPileup(contig, getGenomicPosition(windowPosition), strand, baseConfig.getBases().length);

		WindowCache windowCache;
		switch (strand) {
		case FORWARD:
			windowCache = forwardWindowCache;
			break;

		case REVERSE:
			windowCache = reverseWindowCache;
			break;

		case UNKNOWN:
		default:
			return null;
		}

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(new int[windowCache.baseLength]);
		System.arraycopy(windowCache.baseCount[windowPosition], 0, pileup.getCounts().getBaseCount(), 0, windowCache.baseCount[windowPosition].length);
		pileup.getCounts().setQualCount(new int[windowCache.baseLength][Phred2Prob.MAX_Q]);
		for (int baseI = 0; baseI < windowCache.baseLength; ++baseI) {
			System.arraycopy(windowCache.getQual(windowPosition)[baseI], 0, pileup.getCounts().getQualCount()[baseI], 0, windowCache.getQual(windowPosition)[baseI].length);
		}

		return pileup;
	}

	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		return getCoverage(windowPosition, strand) >= sample.getMinCoverage();
	}

	@Override
	protected void add2WindowCache(int windowPosition, byte base, byte qual, SAMRecord record) {
		WindowCache windowCache = null;
		int baseI = -1;

		if (record.getReadNegativeStrandFlag()) {
			baseI = baseConfig.getComplementBaseI(base);
			windowCache = reverseWindowCache;
		} else {
			baseI = baseConfig.getBaseI(base);
			windowCache = forwardWindowCache;
		}

		// ignore bases that should not be considered - see processAlignmentBlock
		if (baseI < 0) {
			return;
		}
		windowCache.add(windowPosition, baseI, qual);
	}

	@Override
	public boolean adjustWindowStart(int genomicWindowStart) {

		return super.adjustWindowStart(genomicWindowStart);
	}
	
	/**
	 * 
	 * @param record
	 * @throws Exception
	 */
	@Override
	protected void processFilters(final SAMRecord record) {
		FilterContainer filterContainer = forwardFilterContainer;
		if (record.getReadNegativeStrandFlag()) {
			filterContainer = reverseFilterContainer;
		}

		// filter
		// let the filter decide what data they need
		filterContainer.processRecord(record);
	}

}