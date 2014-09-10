package accusa2.filter.feature;

import accusa2.filter.AbstractCountFilter;
import accusa2.filter.FilterConfig;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

public class RareEventFilter extends AbstractCountFilter {

	private int reads;
	private double level;

	public RareEventFilter(final char c, 
			final int reads, 
			final double level, 
			final BaseConfig baseConfig,
			final FilterConfig filterConfig) {
		super(c, baseConfig, filterConfig);
		this.reads = reads;
		this.level = level;
	}

	@Override
	public boolean filter(int variantBaseI, final ParallelPileup parallelPileup) {
		// homo-hetero-morph scenario
		int[] variants = parallelPileup.getVariantBaseIs();
		if (variants.length > 0) {
			int variant = variants[0];

			Pileup pileup = parallelPileup.getPooledPileupA();
			if (parallelPileup.getPooledPileupB().getAlleles().length > 1) {
				pileup = parallelPileup.getPooledPileupB(); 
			}

			int reads = pileup.getBaseCount()[variant];
			double level = (double)reads / (double)pileup.getCoverage();

			if(reads < this.reads || level < this.level) {
				return true;
			}
		}

		return false;
	}

	public int getReads() {
		return reads;
	}

	public double getLevel() {
		return level;
	}

}