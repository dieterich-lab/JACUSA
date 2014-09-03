/**
 * 
 */
package accusa2.pileup.builder;

import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.parameters.SampleParameters;
import accusa2.filter.FilterConfig;
import accusa2.filter.cache.AbstractFilterCount;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.Pileup;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
public class UndirectedPileupBuilder extends AbstractPileupBuilder {

	protected WindowCache windowCache;
	protected AbstractFilterCount[] filterCaches;

	
	protected STRAND strand;
	protected int windowPosition;

	public UndirectedPileupBuilder(
			final AnnotatedCoordinate annotatedCoordinate, 
			final SAMFileReader reader, 
			final int windowSize,
			final BaseConfig baseConfig,
			final FilterConfig filterConfig,
			final SampleParameters parameters) {
		super(annotatedCoordinate, reader, windowSize, baseConfig, parameters);

		windowCache 	= new WindowCache(windowSize, baseConfig.getBases().length);
		filterCaches	= filterConfig.createCache();
		
		strand 			= STRAND.UNKNOWN;
	}

	/*
	@Override
	public Counts[] getFilteredCounts(int windowPosition, STRAND strand) {
		Counts[] counts = new Counts[filterCaches.length];
		for (int i = 0; i < counts.length; ++i) {
			Counts count = pileup.new Counts(new int[windowCache.baseCount.length], new int[windowCache.baseCount.length][Phred2Prob.MAX_Q]);

			// copy base and qual info from cache
			System.arraycopy(windowCache.baseCount[windowPosition], 0, count.getBaseCount(), 0, windowCache.baseCount[windowPosition].length);
			for (int baseI = 0; baseI < windowCache.baseCount[windowPosition].length; ++baseI) {
				// selective copy
				if (count.getBaseCount(baseI) > 0) {
					System.arraycopy(windowCache.getQual(windowPosition)[baseI], 0, count.getQualCount()[baseI], 0, count.getQualCount()[baseI].length);
				}
			}

			counts[i] = count;
		}

		return counts;
	}
	*/
	
	public Counts[] getFilteredCounts(int windowPosition, STRAND strand) {
		Counts[] counts = new Counts[filterCaches.length];
		for (int i = 0; i < counts.length; ++i) {
			counts[i] = pileup.new Counts(windowCache.baseCount[windowPosition], windowCache.qualCount[windowPosition]);
		}

		return counts;
	}
	
	public Pileup getPileup(int windowPosition, STRAND strand) {
		final DefaultPileup pileup = new DefaultPileup(contig, getCurrentGenomicPosition(windowPosition), strand);

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(windowCache.baseCount[windowPosition]);
		pileup.getCounts().setQualCount(windowCache.qualCount[windowPosition]);

		return pileup;
	}

	@Override
	public void clearCache() {
		windowCache.clear();

		for (AbstractFilterCount filterCache : filterCaches) {
			filterCache.getCache().clear();
		}
	}
	
	@Override
	protected void add2Cache(int windowPosition, int baseI, byte qual, SAMRecord record) {
		windowCache.add(windowPosition, baseI, qual);
	}
	
	@Override
	protected void processFilterCache(SAMRecord record) {
		// let the filter decide what data they need
		for(AbstractFilterCount pileupBuilderFilter : filterCaches) {
			if(pileupBuilderFilter != null) {
				pileupBuilderFilter.processRecord(genomicWindowStart, record);
			}
		}
	}
	
	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	@Override
	public boolean isCovered(int windowPosition, STRAND strand) {
		return getCoverage(windowPosition, STRAND.UNKNOWN) >= parameters.getMinCoverage();
	}

	@Override
	public int getCoverage(int windowPosition, STRAND strand) {
		return windowCache.getCoverage(windowPosition);
	}

}