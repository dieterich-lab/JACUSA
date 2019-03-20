package jacusa.filter.factory;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

public class HomozygousFilterFactory extends AbstractFilterFactory<Void> {

	private int sample;
	
	public HomozygousFilterFactory() {
		super('H', "Filter non-homozygous pileup/BAM in sample 1 or 2 (MUST be set to H:1 or H:2). Default: none");
		sample = 0;
	}

	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		for (int i = 1; i < s.length; ++i) {
			switch(i) {
			case 1:
				final int sample = Integer.parseInt(s[1]);
				if (sample == 1 || sample == 2) {
					setSample(sample);
				}
				break;

			default:
				throw new IllegalArgumentException("Invalid argument: " + line);
			}
		}
	}

	@Override
	public DummyFilterFillCache createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		// storage is not needed - done 
		// Low Quality Base Calls are stored in AbstractBuilder 
		return new DummyFilterFillCache(getC());
	}
	
	public final void setSample(final int sample) {
		this.sample = sample;
	}

	public final int getSample() {
		return sample;
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		return new HomozygousFilter(getC());
	}
	
	private class HomozygousFilter extends AbstractStorageFilter<Void> {

		public HomozygousFilter(final char c) {
			super(c);
		}

		@Override
		public boolean filter(final Result result, final Location location,	final AbstractWindowIterator windowIterator) {
			int alleles = 0;
			final ParallelPileup parallelPileup = result.getParellelPileup();
	
			switch (sample) {
	
			case 1:
				alleles = parallelPileup.getPooledPileup1().getAlleles().length;
				break;
	
			case 2:
				alleles = parallelPileup.getPooledPileup2().getAlleles().length;
				break;
	
			default:
				throw new IllegalArgumentException("Unsupported sample! Must be sample 1 or 2 (H:1 or H:2)");
			}
	
			if (alleles > 1) {
				return true;
			}
	
			return false;
		}

	}
	
}