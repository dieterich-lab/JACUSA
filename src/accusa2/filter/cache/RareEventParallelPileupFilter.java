package accusa2.filter.cache;

import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

public class RareEventParallelPileupFilter extends AbstractParallelPileupFilter {

	private int reads;
	private double level;

	public RareEventParallelPileupFilter(char c, int reads, double level) {
		super(c);
		this.reads = reads;
		this.level = level;
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
		this.filtered = new DefaultParallelPileup(parallelPileup);

		// homo-hetero-morph scenario
		int[] variants = parallelPileup.getVariantBases();
		if(variants.length > 0) {
			int variant = variants[0];

			Pileup pileup = parallelPileup.getPooledPileupA();
			if(parallelPileup.getPooledPileupB().getAlleles().length > 1) {
				pileup = parallelPileup.getPooledPileupB(); 
			}

			int reads = pileup.getBaseCount()[variant];
			double level = (double)reads / (double)pileup.getCoverage();

			if(reads < this.reads || level < this.level) {
				return true;
			}
		}

		this.filtered = parallelPileup;
		return false;
	}

	@Override
	public boolean quitFiltering() {
		return filtered == null;
	}

	public int getReads() {
		return reads;
	}

	public double getLevel() {
		return level;
	}

}