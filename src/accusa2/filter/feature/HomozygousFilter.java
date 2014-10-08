package accusa2.filter.feature;

import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

public class HomozygousFilter extends AbstractFilter {

	// TODO make this dependent on Options for files
	private char sample;

	public HomozygousFilter(char c, char sample) {
		super(c);
		this.sample = sample;
	}

	@Override
	public boolean filter(final ParallelPileup parallelPileup) {
		Pileup pileup = null;

		switch(sample) {

		case 'A':
			pileup = parallelPileup.getPooledPileupA();
			break;

		case 'B':
			pileup = parallelPileup.getPooledPileupB();
			break;

		default:
			throw new IllegalArgumentException("Unsupported sample!");
		}

		if(pileup.getAlleles().length > 1) { // TODO make this more lax...
			return true;
		}

		return false;
	}

	public int getSample() {
		return sample;
	}

}