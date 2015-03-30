package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.result.Result;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

public class MinDifferenceFilterFactory extends AbstractFilterFactory<Void> {

	private static final int PROCESS_REPLICATES	= 0; // 1 = pool, 0 = avg.
	private int processReplicates;
	
	private static final int MIN_READS 		= 0;
	private int min_reads;

	private static final double MIN_LEVEL 	= 0.1;
	private double min_level;

	public MinDifferenceFilterFactory(AbstractParameters paramters) {
		super('L', "Min difference filter. Default: " + 
				Integer.toString(PROCESS_REPLICATES) + ":" + Integer.toString(MIN_READS) + ":" + Double.toString(MIN_LEVEL) + " (R:pool:reads:level)");
		processReplicates 		= PROCESS_REPLICATES;
		min_reads 	= MIN_READS;
		min_level 	= MIN_LEVEL;
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		switch (processReplicates) {
		case 0:
			return new MinDiffAvgFilter(getC());

		default:
			return new MinDiffPooledFilter(getC());
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
		this.processReplicates = pool;
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

	private class MinDiffPooledFilter extends AbstractStorageFilter<Void> {

		public MinDiffPooledFilter(final char c) {
			super(c);
		}

		@Override
		public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();
			for (int baseI : parallelPileup.getPooledPileup().getAlleles()) {
				int diffCount = Math.abs(parallelPileup.getPooledPileup1().getCounts().getBaseCount(baseI) - parallelPileup.getPooledPileup2().getCounts().getBaseCount(baseI));
				int total = parallelPileup.getPooledPileup1().getCounts().getBaseCount(baseI) + parallelPileup.getPooledPileup2().getCounts().getBaseCount(baseI);
				
				double diffLevel = (double) diffCount / (double)total;  
				if (diffCount >= min_reads && diffLevel >= min_level) {
					return false;
				}
			}

			return true;
		}

	}

	private class MinDiffAvgFilter extends AbstractStorageFilter<Void> {
		
		public MinDiffAvgFilter(final char c) {
			super(c);
		}

		@Override
		public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();

			for (int baseI : parallelPileup.getPooledPileup().getAlleles()) {
				int count1 = 0;
				for (Pileup pileup : parallelPileup.getPileups1()) {
					count1 += pileup.getCounts().getBaseCount(baseI);
				}
				double avgCount1 = (double)count1 / parallelPileup.getPileups1().length;
				int count2 = 0;
				for (Pileup pileup : parallelPileup.getPileups2()) {
					count2 += pileup.getCounts().getBaseCount(baseI);
				}
				double avgCount2 = (double)count2 / parallelPileup.getPileups2().length;

				double totalAvgCount = avgCount1 + avgCount2;
				double avgDiffCount = Math.abs(avgCount1 -  avgCount2);
				double avgDiffLevel = avgDiffCount / (double)totalAvgCount;
				if (avgDiffCount >= (double)min_reads && avgDiffLevel >= min_level) {
					return false;
				}
			}

			return true;
		}
		
		
		
	}	

}