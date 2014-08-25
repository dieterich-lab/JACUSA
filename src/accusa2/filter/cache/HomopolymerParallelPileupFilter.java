package accusa2.filter.cache;

import accusa2.cli.Parameters;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

/**
 * @author mpiechotta
 *
 */
public class HomopolymerParallelPileupFilter extends AbstractParallelPileupFilter {

	private int length;
	private int distance;

	private Parameters parameters;

	public HomopolymerParallelPileupFilter(char c, int length, int distance, Parameters parameters) {
		super(c);
		this.length = length;
		this.distance = distance;
		this.parameters = parameters;
	}

	// FIXME what is a variant base - how should we filter?
	// FIXME is this still the case??? after filter filteredPileups lost inside pileup
	// what if AAAA VS GGGG - same number?
	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		this.filtered = new DefaultParallelPileup(parallelPileup);

		int[] alleles = parallelPileup.getPooledPileup().getAlleles();

		// determine the least abundant variant
		for(int base : alleles) {
			int count = parallelPileup.getPooledPileup().getBaseCount()[base];
			Pileup[] filteredPileups1 = applyFilter(base, parallelPileup.getPileupsA());
			Pileup[] filteredPileups2 = applyFilter(base, parallelPileup.getPileupsB());

			// if nothing was filtered
			if(filteredPileups1 == null && filteredPileups2 == null) {
				continue;
			}
			filtered = new DefaultParallelPileup(filteredPileups1, filteredPileups2);
			int filteredCount = filtered.getPooledPileup().getBaseCount()[base];
			
			if((double)filteredCount / (double)count <= 0.5) {
				return true;
			}
		}

		this.filtered = parallelPileup;
		return false;
	}

	/**
	 * null if filter did not change anything
	 * @param pileups
	 * @return
	 */
	private Pileup[] applyFilter(int variantBase, DefaultPileup[] pileups) {
		DefaultPileup[] filtered = new DefaultPileup[pileups.length];

		boolean processed = false;
		for(int i = 0; i < pileups.length; ++i) {
			filtered[i] = new DefaultPileup(pileups[i]);

			DefaultPileup pileup = parameters.getFilterConfig().getFilteredPileup(getC(), filtered[i]);
			if(pileup != null) { 
				filtered[i].substractPileup(variantBase, pileup);
				processed = true;
			}
		}

		return processed ? filtered : null;
	}

	public int getLength() {
		return length;
	}

	public int getDistance() {
		return distance;
	}

}
