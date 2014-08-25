package accusa2.filter.cache;

import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

public class HomozygousParallelPileupFilter extends AbstractParallelPileupFilter {

	private int sample;

	public HomozygousParallelPileupFilter(char c, int sample) {
		super(c);
		this.sample = sample;
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
		this.filtered = null;
		Pileup pileup = null;

		switch(sample) {

		case 1:
			pileup = parallelPileup.getPooledPileupA();
			break;

		case 2:
			pileup = parallelPileup.getPooledPileupB();
			break;

		default:
			throw new IllegalArgumentException("Unsupported sample!");
		}

		if(pileup.getAlleles().length > 1) { // make this more lax...
			return true;
		}

		this.filtered = parallelPileup;
		return false;
	}

	@Override
	public boolean quitFiltering() {
		return filtered == null;
	}

	public int getSample() {
		return sample;
	}

}