package jacusa.filter.factory;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.result.Result;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

public class HomozygousFilterFactory extends AbstractFilterFactory<Void> {

	private int sample;

	public HomozygousFilterFactory() {
		super('H', "Filter non-homozygous pileup/BAM (1 or 2). Default: none");
		sample = 0;
	}

	@Override
	public void processCLI(final String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		final int sample = Integer.parseInt(s[1]);
		if (sample == 1 || sample == 2) {
			setSample(sample);
			return;
		}
		throw new IllegalArgumentException("Invalid argument " + sample);
	}

	@Override
	public DummyFilterFillCache createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		// storage is not needed
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
			final ParallelPileup parallelPileup = result.getParellelPileup();
			Pileup pileup = null;
	
			switch (sample) {
	
			case 1:
				pileup = parallelPileup.getPooledPileup1();
				break;
	
			case 2:
				pileup = parallelPileup.getPooledPileup2();
				break;
	
			default:
				throw new IllegalArgumentException("Unsupported sample!");
			}
	
			if (pileup.getAlleles().length > 1) {
				return true;
			}
	
			return false;
		}

	}
}