package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.Counts;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

public class RareEventFilterFactory extends AbstractFilterFactory<Void> {

	private static final int POOL			= 1;
	private int pool;
	
	private static final int MIN_READS 		= 2;
	private int min_reads;

	private static final double MIN_LEVEL 	= 0.1;
	private double min_level;

	public RareEventFilterFactory(AbstractParameters paramters) {
		super('R', "Rare event filter. Default: " + 
				Integer.toString(POOL) + ":" + Integer.toString(MIN_READS) + ":" + Double.toString(MIN_LEVEL) + " (R:pool:reads:level)");
		pool 		= POOL;
		min_reads 	= MIN_READS;
		min_level 	= MIN_LEVEL;
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		switch (pool) {
		case 0:
			return new RareFilter(getC());

		default:
			return new PoolRareFilter(getC());
		}
	}

	@Override
	public AbstractFilterStorage<Void> createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}

	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format R:pool:reads:level 
		for (int i = 1; i < s.length; ++i) {

			switch(i) {

			case 1:
				setPool(Integer.valueOf(s[i]));
				break;
			
			case 2:
				setReads(Integer.valueOf(s[i]));
				break;

			case 3:
				setLevel(Double.valueOf(s[i]));
				break;

			default:
				throw new IllegalArgumentException("Invalid argument " + s[i]);
			}
		}
	}

	public void setPool(final int pool) {
		this.pool = pool;
	}

	public final void setReads(final int reads) {
		this.min_reads = reads;
	}

	public final int getReads() {
		return min_reads;
	}

	public final void setLevel(final double level) {
		this.min_level = level;
	}

	public final double getLevel() {
		return min_level;
	}

	private boolean isValid(final int baseI, final int coverage, final Counts counts) {
		int reads = counts.getBaseCount(baseI);
		double level = (double)reads / (double)coverage;

		if (level >= this.min_level && reads >= this.min_reads) {
			return true;
		}

		return false;
	}
	
	private class PoolRareFilter extends AbstractStorageFilter<Void> {

		public PoolRareFilter(final char c) {
			super(c);
		}

		@Override
		public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();
			for (int baseI : parallelPileup.getPooledPileup1().getAlleles()) {
				if (! isValid(baseI, parallelPileup.getPooledPileup1().getCoverage(), parallelPileup.getPooledPileup1().getCounts())) {
					return true;
				}
			}
			for (int baseI : parallelPileup.getPooledPileup2().getAlleles()) {
				if (! isValid(baseI, parallelPileup.getPooledPileup2().getCoverage(), parallelPileup.getPooledPileup2().getCounts())) {
					return true;
				}
			}

			return false;
		}

	}

	private class RareFilter extends AbstractStorageFilter<Void> {
		
		public RareFilter(final char c) {
			super(c);
		}

		@Override
		public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();
			if (filter(parallelPileup.getPooledPileup1().getAlleles(), parallelPileup.getPileups1()) || 
					filter(parallelPileup.getPooledPileup2().getAlleles(), parallelPileup.getPileups2())) {
				return true;
			}

			return false;
		}

		public boolean filter(int[] bases, Pileup[] pileups) {
			for (Pileup pileup : pileups) {
				for (int baseI : bases) {
					if(! isValid(baseI, pileup.getCoverage(), pileup.getCounts())) {
						return false;
					}
				}
			}
	
			return true;
		}
	}	

}