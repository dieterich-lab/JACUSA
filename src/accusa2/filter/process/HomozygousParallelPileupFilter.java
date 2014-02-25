package accusa2.filter.process;

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
		this.filteredParallelPileup = null;
		Pileup pileup = null;

		switch(sample) {

		case 1:
			pileup = parallelPileup.getPooledPileup1();
			break;

		case 2:
			pileup = parallelPileup.getPooledPileup2();
			break;

		default:
			throw new IllegalArgumentException("Unsupported sample!");
		}

		if(pileup.getAlleles().length > 2) {
			return true;
		}

		this.filteredParallelPileup = parallelPileup;
		return false;
	}

	@Override
	public boolean quitFiltering() {
		return filteredParallelPileup == null;
	}

	public int getSample() {
		return sample;
	}

}
