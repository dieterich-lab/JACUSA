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
		filteredParallelPileup = new ParallelPileup(parallelPileup);

		int[] variants = filteredParallelPileup.getVariantBases();
		if(variants.length == 0 || filteredParallelPileup.isHoHo()) {
			return false; // FIXME all other cases are currently ignored
		}
		int variant = variants[0]; 

		int count1 = filteredParallelPileup.getPooledPileup1().getBaseCount()[variant];
		Pileup[] filteredPileups1 = applyFilter(variant, filteredParallelPileup.getPileups1());

		int count2 = filteredParallelPileup.getPooledPileup2().getBaseCount()[variant];
		Pileup[] filteredPileups2 = applyFilter(variant, filteredParallelPileup.getPileups2());

		// if nothing was filtered
		if(count1 == 0 && count2 == 0) {
			return true;
		}
		
		int filteredCount = 0;
		int count = 0;
		if(count1 > 0) {
			filteredParallelPileup.setPileups1(filteredPileups1);
			filteredCount = filteredParallelPileup.getPooledPileup1().getBaseCount()[variant];
			count = count1;
		}
		if(count2 > 0) {
			filteredParallelPileup.setPileups2(filteredPileups2);
			filteredCount = filteredParallelPileup.getPooledPileup2().getBaseCount()[variant];
			count = count2;
		}

		if((double)filteredCount / (double)count <= 0.5) {
			return true;
		}

		filteredParallelPileup = parallelPileup;
		return false;
	}

	/**
	 * FIXME check this
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
