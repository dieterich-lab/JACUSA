/**
 * 
 */
package accusa2.pileup.builder;

import net.sf.samtools.AlignmentBlock;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractPileupBuilderFilterCache;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.AnnotatedCoordinate;

/**
 * @author michael
 *
 */
public class UndirectedPileupBuilder extends AbstractPileupBuilder {

	protected DefaultWindowCache windowCache;
	
	protected DefaultPileup pileup;
	protected STRAND strand;
	protected int windowPosition;

	public UndirectedPileupBuilder(final AnnotatedCoordinate annotatedCoordinate, final SAMFileReader reader, final Parameters parameters) {
		super(annotatedCoordinate, reader, parameters);
		int windowSize = parameters.getWindowSize();

		windowCache = new DefaultWindowCache(windowSize, parameters.getBaseConfig().getBases().length);
		pileup = new DefaultPileup();
	}

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
	
	public Pileup getPileup(int windowPosition, STRAND strand) {
		final DefaultPileup pileup = new DefaultPileup(contig, getCurrentGenomicPosition(windowPosition), strand);

		// copy base and qual info from cache
		pileup.getCounts().setBaseCount(new int[windowCache.baseLength]);
		System.arraycopy(windowCache.baseCount[windowPosition], 0, pileup.getBaseCount(), 0, windowCache.baseCount[windowPosition].length);
		pileup.getCounts().setQualCount(new int[windowCache.baseLength][Phred2Prob.MAX_Q]);
		for (int baseI = 0; baseI < windowCache.baseLength; ++baseI) {
			System.arraycopy(windowCache.getQual(windowPosition)[baseI], 0, pileup.getQualCount()[baseI], 0, pileup.getQualCount()[baseI].length);
		}
		
		return pileup;
	}
	
	@Override
	public void clearCache() {
		windowCache.clear();

		for (AbstractPileupBuilderFilterCache filterCache : filterCaches) {
			filterCache.getCache().clear();
		}
	}
	
	@Override
	protected void processAlignmentBlock(final SAMRecord record, final AlignmentBlock alignmentBlock) {
		int readPosition = alignmentBlock.getReadStart() - 1;
		int genomicPosition = alignmentBlock.getReferenceStart();

		for (int offset = 0; offset < alignmentBlock.getLength(); ++offset) {
			final int baseI = parameters.getBaseConfig().getBaseI(record.getReadBases()[readPosition + offset]);
			final byte qual = record.getBaseQualities()[readPosition + offset];

			if(qual >= parameters.getMinBASQ() && baseI != -1) {
				// speedup: if windowPosition == -1 the remaining part of the read will be outside of the windowCache
				// ignore the overhanging part of the read until it overlaps with the window cache
				final int windowPosition = convertGenomicPosition2WindowPosition(genomicPosition + offset);
				if (windowPosition < 0) {
					return;
				}

				windowCache.add(windowPosition, baseI, qual);
			}
		}
	}

	/**
	 * 
	 * @param windowPosition
	 * @return
	 */
	public boolean isCovered(int windowPosition) {
		return getCoverage(windowPosition, STRAND.UNKNOWN) >= parameters.getMinCoverage();
	}

	public int getCoverage(int windowPosition, STRAND strand) {
		return windowCache.getCoverage(windowPosition);
	}

}