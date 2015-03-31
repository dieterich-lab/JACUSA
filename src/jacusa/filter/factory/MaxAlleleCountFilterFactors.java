package jacusa.filter.factory;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

public class MaxAlleleCountFilterFactors extends AbstractFilterFactory<Void> {

	private static int ALLELE_COUNT = 2;
	private int alleleCount;
	
	public MaxAlleleCountFilterFactors() {
		super(
				'M', 
				"Max allowed alleles per parallel pileup. Default: "+ ALLELE_COUNT);
		alleleCount = ALLELE_COUNT;
	}
	
	@Override
	public DummyFilterFillCache createFilterStorage(
			WindowCoordinates windowCoordinates,
			SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		return new MaxAlleleFilter(getC());
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			return;
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		// format D:distance:minRatio:minCount
		for (int i = 1; i < s.length; ++i) {
			switch(i) {
			case 1:
				final int alleleCount = Integer.valueOf(s[i]);
				if (alleleCount < 0) {
					throw new IllegalArgumentException("Invalid allele count " + line);
				}
				this.alleleCount = alleleCount;
				break;
				
			default:
				throw new IllegalArgumentException("Invalid argument: " + line);
			}
		}
	}
	
	private class MaxAlleleFilter extends AbstractStorageFilter<Void> {
		
		public MaxAlleleFilter(final char c) {
			super(c);
		}
		
		@Override
		public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();
			return parallelPileup.getPooledPileup().getAlleles().length > alleleCount;
		}
	}
	
}
