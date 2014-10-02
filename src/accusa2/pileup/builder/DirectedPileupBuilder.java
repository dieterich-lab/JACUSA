package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.cache.AbstractCountFilterCache;
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

	protected AbstractCountFilterCache[] forwardFilterCaches;
	protected AbstractCountFilterCache[] reverseFilterCaches;

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
	}

	@Override
	public void clearCache() {
		forwardWindowCache.clear();
		reverseWindowCache.clear();

		for (int i = 0; i < forwardFilterCaches.length; ++i) {
			forwardFilterCaches[i].getCache().clear();
			reverseFilterCaches[i].getCache().clear();
		}
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
		Counts[] counts;
		AbstractCountFilterCache tmpCache[];

		switch (strand) {
		case FORWARD:
			counts = new Counts[forwardFilterCaches.length];
			tmpCache = forwardFilterCaches;
			break;

		case REVERSE:
			counts = new Counts[reverseFilterCaches.length];
			tmpCache = reverseFilterCaches;
			break;
		
		case UNKNOWN:
		default:
			return null;
		}

		for (int i = 0; i < counts.length; ++i) {
			counts[i] = pileup.new Counts(
					tmpCache[i].getCache().baseCount[windowPosition], 
					tmpCache[i].getCache().qualCount[windowPosition]);
		}

		return counts;
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
	protected void add2Cache(int windowPosition, byte base, byte qual, SAMRecord record) {
		if (record.getReadNegativeStrandFlag()) {
			int baseI = baseConfig.getComplementBaseI(base);
			// ignore bases that should not be considered - see processAlignmentBlock
			if (baseI < 0) {
				return;
			}
			reverseWindowCache.add(windowPosition, baseI, qual);
		} else {
			int baseI = baseConfig.getBaseI(base);
			// ignore bases that should not be considered - see processAlignmentBlock
			if (baseI < 0) {
				return;
			}
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
		AbstractCountFilterCache[] filterCaches = forwardFilterCaches;
		if (record.getReadNegativeStrandFlag()) {
			filterCaches = reverseFilterCaches;
		}

		// filter
		// let the filter decide what data they need
		for (AbstractCountFilterCache pileupBuilderFilter : filterCaches) {
			if(pileupBuilderFilter != null) {
				pileupBuilderFilter.processRecord(genomicWindowStart, record);
			}
		}
	}

}