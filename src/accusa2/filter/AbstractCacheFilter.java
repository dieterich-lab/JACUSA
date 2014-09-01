package accusa2.filter;

import accusa2.cli.Parameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.Counts;

public abstract class AbstractCacheFilter extends AbstractParallelPileupFilter {

	protected final int filterI;
	protected final BaseConfig baseConfig;
		
	public AbstractCacheFilter(char c, Parameters parameters) {
		super(c);
		filterI = parameters.getFilterConfig().c2i(c);
		baseConfig = parameters.getBaseConfig();
		
	}

	// todo ORDER RESULTS [0] SHOULD BE THE VARIANT TO TEST AGAINST
	protected int[] getVariantBaseI(ParallelPileup parallelPileup) {
		int[] variantBaseIs = parallelPileup.getVariantBases();

		// A | G
		// define all non-reference bases as potential variants
		if (DefaultParallelPileup.isHoHo(parallelPileup)) {
			final char refBase = parallelPileup.getPooledPileup().getRefBase();
			if (refBase == 'N') {
				return new int[0];
			}
			final int refBaseI = baseConfig.getBaseI((byte)refBase);

			// find non-reference base(s)
			int i = 0;
			final int[] tmp = new int[variantBaseIs.length];
			for (final int baseI : variantBaseIs) {
				if (baseI != refBaseI) {
					tmp[i] = baseI;
					++i;
				}
			}
			final int[] variants = new int[i];
			System.arraycopy(tmp, 0, variants, 0, i);
			return variants;
		}

		// A | AG
		if (variantBaseIs.length == 1) {
			return variantBaseIs;
		}

		// AG | AG
		if (DefaultParallelPileup.isHeHe(parallelPileup)) {
			parallelPileup.getPooledPileup().getAlleles();
		}

		return new int[0];
	}

	/**
	 * null if filter did not change anything
	 * @param extendedPileups
	 * @return
	 */
	final protected Pileup[] applyFilter(final int variantBaseI, final Pileup[] pileups, final Counts[] counts) {
		final Pileup[] filtered = new DefaultPileup[pileups.length];

		boolean processed = false;
		for(int i = 0; i < pileups.length; ++i) {
			filtered[i] = new DefaultPileup(pileups[i]);
			Counts count = counts[i];
			if(count != null) { 
				filtered[i].getCounts().substract(variantBaseI, count);
				processed = true;
			}
		}

		return processed ? filtered : null;
	}

	final protected ParallelPileup applyFilter(final int variantBaseI, final ParallelPileup parallelPileup) {
		Pileup[] pileupsA = applyFilter(variantBaseI, parallelPileup.getPileupsA(), parallelPileup.getFilterCountsA()[filterI]);
		Pileup[] pileupsB = applyFilter(variantBaseI, parallelPileup.getPileupsB(), parallelPileup.getFilterCountsB()[filterI]);

		if (pileupsA == null && pileupsB == null) {
			return null;
		}

		ParallelPileup filtered = new DefaultParallelPileup(parallelPileup.getNA(), parallelPileup.getNB());
		filtered.setContig(parallelPileup.getContig());
		filtered.setPosition(parallelPileup.getPosition());
		filtered.setStrand(parallelPileup.getStrand());

		if (pileupsA == null) {
			filtered.setPileupsA(parallelPileup.getPileupsA());
		} else {
			filtered.setPileupsA(pileupsA);
		}

		if (pileupsB == null) {
			filtered.setPileupsB(parallelPileup.getPileupsB());
		} else {
			filtered.setPileupsB(pileupsB);
		}
	
		return filtered;
	}

}