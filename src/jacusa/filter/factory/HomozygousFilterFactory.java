package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public class HomozygousFilterFactory extends AbstractFilterFactory<Void> {

	private int sample;

	public HomozygousFilterFactory(AbstractParameters parameters) {
		super('H', "Filter non-homozygous pileup/BAM (1 or 2). Default: none");
		sample = 0;
	}
	
	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		int sample = Integer.parseInt(s[1]);
		if (sample == 1 || sample == 2) {
			setSample(sample);
			return;
		}
		throw new IllegalArgumentException("Invalid argument " + sample);
	}

	@Override
	public AbstractFilterStorage<Void> createFilterStorage() {
		return new DummyFilterFillCache(c);
	}
	
	public final void setSample(int sample) {
		this.sample = sample;
	}

	public final int getSample() {
		return sample;
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		return new HomozygousFilter(c);
	}

	private class HomozygousFilter extends AbstractStorageFilter<Void> {

		public HomozygousFilter(final char c) {
			super(c);
			
		}
		
		@Override
		public boolean filter(ParallelPileup parallelPileup, Location location,	AbstractWindowIterator windowIterator) {
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
