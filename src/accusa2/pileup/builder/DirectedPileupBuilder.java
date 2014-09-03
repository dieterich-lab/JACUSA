package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.cache.AbstractFilterCount;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
public class DirectedPileupBuilder extends AbstractPileupBuilder {

	protected WindowCache forwardWindowCache;
	protected WindowCache reverseWindowCache;

	protected AbstractFilterCount[] forwardFilterCaches;
	protected AbstractFilterCount[] reverseFilterCaches;
	
	protected STRAND strand;
	protected int windowPosition;
	
	public DirectedPileupBuilder(
			final AnnotatedCoordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final SampleParameters sample,
			final AbstractParameters parameters) {
		super(annotatedCoordinate, reader, sample, parameters);

		forwardWindowCache = new WindowCache(windowSize, baseConfig.getBases().length);
		reverseWindowCache = new WindowCache(windowSize, baseConfig.getBases().length);

		forwardFilterCaches = parameters.getFilterConfig().createCache();
		reverseFilterCaches = parameters.getFilterConfig().createCache();

		strand = STRAND.UNKNOWN;
	}

	@Override
	public void clearCache() {
		forwardWindowCache.clear();
		reverseWindowCache.clear();

		// always consider forward strand first
		strand = STRAND.FORWARD;
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
	public Counts[] getFilteredCounts(int windowPosition, STRAND strand) {
		switch (strand) {
		case FORWARD:
			return null;

		case REVERSE:
			return null;
		
		case UNKNOWN:
		default:
			return null;
		}
	}

	@Override
	public Pileup getPileup(int windowPosition, STRAND strand) {
		final DefaultPileup pileup = new DefaultPileup(contig, getCurrentGenomicPosition(windowPosition), strand);

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
			return pileup;
		}

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(new int[windowCache.baseLength]);
		System.arraycopy(windowCache.baseCount[windowPosition], 0, pileup.getBaseCount(), 0, windowCache.baseCount[windowPosition].length);
		pileup.getCounts().setQualCount(new int[windowCache.baseLength][Phred2Prob.MAX_Q]);
		for (int baseI = 0; baseI < windowCache.baseLength; ++baseI) {
			System.arraycopy(windowCache.getQual(windowPosition)[baseI], 0, pileup.getQualCount()[baseI], 0, windowCache.getQual(windowPosition)[baseI].length);
		}

		return pileup;
	}

	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		return getCoverage(windowPosition, strand) >= sample.getMinCoverage();
	}

	@Override
	protected void add2Cache(int windowPosition, int baseI, byte qual, SAMRecord record) {
		if (record.getReadNegativeStrandFlag()) {
			reverseWindowCache.add(windowPosition, baseI, qual);
		} else {
			forwardWindowCache.add(windowPosition, baseI, qual);
		}
	}
	
	/**
	 * 
	 * @param record
	 * @throws Exception
	 */
	@Override
	protected void processFilterCache(final SAMRecord record) {
		AbstractFilterCount[] filterCaches = forwardFilterCaches;
		if (record.getReadNegativeStrandFlag()) {
			filterCaches = reverseFilterCaches;
		}

		// filter
		// let the filter decide what data they need
		for(AbstractFilterCount pileupBuilderFilter : filterCaches) {
			if(pileupBuilderFilter != null) {
				pileupBuilderFilter.processRecord(genomicWindowStart, record);
			}
		}
	}

}