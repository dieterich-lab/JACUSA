package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public class RareEventFilterFactory extends AbstractFilterFactory<Void> {

	private int reads 		= 2;
	private double level 	= 0.1;
	// TODO private boolean pool	= false;

	public RareEventFilterFactory(AbstractParameters paramters) {
		super('R', "");
		desc = "Rare event filter. Default: reads:level " + Integer.toString(reads) + ":" + Double.toString(level);
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		return new RareFilter(c, reads, level);
	}

	@Override
	public AbstractFilterStorage<Void> createFilterStorage() {
		return new DummyFilterFillCache(c);
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format R:reads:level 
		for (int i = 1; i < s.length; ++i) {

			switch(i) {
			case 1:
				setReads(Integer.valueOf(s[i]));
				break;

			case 2:
				setLevel(Double.valueOf(s[i]));
				break;

			default:
				throw new IllegalArgumentException("Invalid argument " + s[i]);
			}
		}
	}

	public final void setReads(int reads) {
		this.reads = reads;
	}

	public final int getReads() {
		return reads;
	}

	public final void setLevel(double level) {
		this.level = level;
	}

	public final double getLevel() {
		return level;
	}

	private class RareFilter extends AbstractStorageFilter<Void> {

		private int reads;
		private double level;

		public RareFilter(final char c, final int reads, final double level) {
			super(c);

			this.reads = reads;
			this.level = level;
		}

		@Override
		public boolean filter(ParallelPileup parallelPileup, Location location, AbstractWindowIterator windowIterator) {
			// homo-hetero-morph scenario
			int[] variants = parallelPileup.getVariantBaseIs();
			if (variants.length > 0) {
				int variant = variants[0];

				Pileup pileup = parallelPileup.getPooledPileup1();
				if (parallelPileup.getPooledPileup2().getAlleles().length > 1) {
					pileup = parallelPileup.getPooledPileup2(); 
				}

				int reads = pileup.getCounts().getBaseCount()[variant];
				double level = (double)reads / (double)pileup.getCoverage();

				if (reads < this.reads || level < this.level) {
					return true;
				}
			}

			return false;
		}
		
	}

}