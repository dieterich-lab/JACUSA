package accusa2.filter.cache;

import accusa2.cli.Parameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.Counts;

public abstract class AbstractParallelPileupFilter {

	private char c;
	protected ParallelPileup filtered;

	public AbstractParallelPileupFilter(char c) {
		this.c = c;
	}

	public abstract boolean filter(ParallelPileup parallelPileup);
	public boolean quitFiltering() {
		return false;
	}

	public ParallelPileup getFilteredParallelPileup() {
		return filtered;
	}

	public final char getC() {
		return c;
	}

	protected int[] getVariantBaseI(ParallelPileup parallelPileup) {
		int[] variantBaseIs = parallelPileup.getVariantBases();

		// TODO understand this
		BaseConfig baseConfig = Parameters.getInstance().getBaseConfig();

		// define all non-reference bases as potential variants
		if (DefaultParallelPileup.isHoHo(parallelPileup)) {
			char refBase = parallelPileup.getPooledPileup().getReferenceBase();
			int refBaseI = baseConfig.getBaseI((byte)refBase);

			
			for (int baseI : variantBaseIs) {
				if (baseI != refBaseI) {
					return baseI; //
				}
			}
		}

		
		
		return null;
	}

	/**
	 * null if filter did not change anything
	 * @param extendedPileups
	 * @return
	 */
	protected Pileup[] applyFilter(int variantBaseI, Pileup[] pileups, Counts[] counts) {
		Pileup[] filtered = new DefaultPileup[pileups.length];

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
	
}