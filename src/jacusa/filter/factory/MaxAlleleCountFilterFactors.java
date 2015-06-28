package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
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
	private AbstractParameters parameters;
	private boolean strict;
	
	public MaxAlleleCountFilterFactors(AbstractParameters parameters) {
		super(
				'M', 
				"Max allowed alleles per parallel pileup. Default: "+ ALLELE_COUNT);
		alleleCount = ALLELE_COUNT;
		this.parameters = parameters;
		strict = parameters.collectLowQualityBaseCalls();
	}
	
	@Override
	public DummyFilterFillCache createFilterStorage(
			WindowCoordinates windowCoordinates,
			SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		if (strict) {
			return new MaxAlleleStrictFilter(getC());
		}
		return new MaxAlleleFilter(getC());
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			return;
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		for (int i = 1; i < s.length; ++i) {
			switch(i) {
			case 1:
				final int alleleCount = Integer.valueOf(s[i]);
				if (alleleCount < 0) {
					throw new IllegalArgumentException("Invalid allele count " + line);
				}
				this.alleleCount = alleleCount;
				break;
		
			case 2:
				if (! s[i].equals("strict")) {
					throw new IllegalArgumentException("Did you mean strict? " + line);
				}
				parameters.collectLowQualityBaseCalls(true);
				strict = true;
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
	
	private class MaxAlleleStrictFilter extends AbstractStorageFilter<Void> {
		
		public MaxAlleleStrictFilter(final char c) {
			super(c);
		}
		
		@Override
		public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
			return windowIterator.getAlleleCount(location) > alleleCount;
		}
	}
	
}
