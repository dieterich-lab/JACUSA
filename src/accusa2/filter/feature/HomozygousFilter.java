package accusa2.filter.feature;

import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

public class HomozygousFilter extends AbstractFeatureFilter {

	private int sample;

	public HomozygousFilter(char c, int sample) {
		super(c);
		this.sample = sample;
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
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

		return false;
	}

	public int getSample() {
		return sample;
	}

}