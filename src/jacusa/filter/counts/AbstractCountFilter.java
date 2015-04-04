package jacusa.filter.counts;

import jacusa.filter.FilterConfig;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Counts;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;

public abstract class AbstractCountFilter {

	protected final BaseConfig baseConfig;
	protected final FilterConfig filterConfig;

	public AbstractCountFilter(final BaseConfig baseConfig, final FilterConfig filterConfig) {
		this.baseConfig 	= baseConfig;
		this.filterConfig 	= filterConfig;
	}

	// ORDER RESULTS [0] SHOULD BE THE VARIANTs TO TEST
	public int[] getVariantBaseIs(final ParallelPileup parallelPileup) {
		final int[] variantBasesIs = parallelPileup.getVariantBaseIs();
		final int[] allelesIs = parallelPileup.getPooledPileup().getAlleles();
		final char refBase = parallelPileup.getPooledPileup().getRefBase();

		// A | G
		// define all non-reference bases as potential variants
		if (DefaultParallelPileup.isHoHo(parallelPileup)) {
			if (refBase == 'N') {
				return new int[0];
			}
			final int refBaseI = baseConfig.getBaseI((byte)refBase);

			// find non-reference base(s)
			int i = 0;
			final int[] tmp = new int[allelesIs.length];
			for (final int baseI : allelesIs) {
				if (baseI != refBaseI) {
					tmp[i] = baseI;
					++i;
				}
			}
			final int[] ret = new int[i];
			System.arraycopy(tmp, 0, ret, 0, i);
			return ret;
		}

		// TODO sort variant by occurrence
		// A | AG
		if (variantBasesIs.length >= 1) {
			return variantBasesIs;
		}


		// sample1: AG | AG AND sample2: AGC |AGC
		// return allelesIs;
		return new int[0];
	}
	
	/**
	 * null if filter did not change anything
	 * @param extendedPileups
	 * @return
	 */
	protected Pileup[] applyFilter(final int variantBaseI, final Pileup[] pileups, final Counts[] counts) {
		final Pileup[] filtered = new DefaultPileup[pileups.length];

		// indicates if something has been filtered
		boolean processed = false;
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			filtered[pileupI] = new DefaultPileup(pileups[pileupI]);
			final Counts count = counts[pileupI];
			if (count != null) { 
				filtered[pileupI].getCounts().substract(variantBaseI, count);
				processed = true;
			}
		}

		return processed ? filtered : null;
	}

	final protected ParallelPileup applyFilter(final int variantBaseI, final ParallelPileup parallelPileup, Counts[] counts1, Counts[] counts2) {
		final Pileup[] filteredPileups1 = applyFilter(variantBaseI, parallelPileup.getPileups1(), counts1);
		final Pileup[] filteredPileups2 = applyFilter(variantBaseI, parallelPileup.getPileups2(), counts2);

		if (filteredPileups1 == null && filteredPileups2 == null) {
			// nothing has been filtered
			return null;
		}

		final ParallelPileup filteredParallelPileup = new DefaultParallelPileup(parallelPileup.getN1(), parallelPileup.getN2());
		filteredParallelPileup.setContig(parallelPileup.getContig());
		filteredParallelPileup.setStart(parallelPileup.getStart());
		filteredParallelPileup.setStrand(parallelPileup.getStrand());

		if (filteredPileups1 == null) {
			filteredParallelPileup.setPileups1(parallelPileup.getPileups1());
		} else {
			filteredParallelPileup.setPileups1(filteredPileups1);
		}

		if (filteredPileups2 == null) {
			filteredParallelPileup.setPileups2(parallelPileup.getPileups2());
		} else {
			filteredParallelPileup.setPileups2(filteredPileups2);
		}

		return filteredParallelPileup;
	}

	/**
	 * Apply filter on each variant base
	 */
	public boolean filter(final int[] variantBaseIs, ParallelPileup parallelPileup, Counts[] counts1, Counts[] counts2) {
		// final int[] variantBaseIs = getVariantBaseIs(parallelPileup);

		for (int variantBaseI : variantBaseIs) {
			if (filter(variantBaseI, parallelPileup, counts1, counts2)) {
				return true;
			}
		}

		return false;
	}

	protected abstract boolean filter(final int variantBaseI, final ParallelPileup parallelPileup, Counts[] counts1, Counts[] counts2);

}