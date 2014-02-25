package accusa2.filter.process;

import accusa2.cli.Parameters;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

/**
 * @author mpiechotta
 *
 */
// CHECKED
public class DistanceParallelPileupFilter extends AbstractParallelPileupFilter {

	private int filterDistance;
	private Parameters parameters;

	public DistanceParallelPileupFilter(char c, int filterDistance, Parameters parameters) {
		super(c);
		this.filterDistance = filterDistance;
		this.parameters = parameters;
	}

	// FIXME what is a variant base - how should we filter?
	// FIXME is this still the case??? after filter filteredPileups lost inside pileup
	// what if AAAA VS GGGG - same number?
	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		this.filteredParallelPileup = parallelPileup;

		int[] alleles = parallelPileup.getPooledPileup().getAlleles();

		// determine the least abundant variant
		for(int base : alleles) {
			int count = parallelPileup.getPooledPileup().getBaseCount()[base];
			Pileup[] filteredPileups1 = applyFilter(base, parallelPileup.getPileups1());
			Pileup[] filteredPileups2 = applyFilter(base, parallelPileup.getPileups2());

			// if nothing was filtered
			if(filteredPileups1 == null && filteredPileups2 == null) {
				continue;
			}
			filteredParallelPileup = new ParallelPileup(filteredPileups1, filteredPileups2);
			int filteredCount = filteredParallelPileup.getPooledPileup().getBaseCount()[base];
			
			if((double)filteredCount / (double)count <= 0.5) {
				return true;
			}
		}

		return false;
	}

	/**
	 * null if filter did not change anything
	 * @param pileups
	 * @return
	 */
	private Pileup[] applyFilter(int variantBase, Pileup[] pileups) {
		Pileup[] filtered = new Pileup[pileups.length];

		boolean processed = false;
		for(int i = 0; i < pileups.length; ++i) {
			filtered[i] = new Pileup(pileups[i]);

			Pileup pileup = parameters.getPileupBuilderFilters().getFilteredPileup(getC(), filtered[i]);
			if(pileup != null) { 
				filtered[i].substractPileup(variantBase, pileup);
				processed = true;
			}
		}

		return processed ? filtered : null;
	}

	public int getDistance() {
		return filterDistance;
	}
	
}
