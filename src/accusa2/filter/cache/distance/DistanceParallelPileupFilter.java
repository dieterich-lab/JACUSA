package accusa2.filter.cache.distance;

import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractParallelPileupFilter;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
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
		filtered = new DefaultParallelPileup(parallelPileup);

		int[] variants = filtered.getVariantBases();
		if(variants.length == 0 || DefaultParallelPileup.isHoHo(filtered)) {
			return false; // FIXME all other cases are currently ignored
		}
		int variant = variants[0]; 

		int count1 = filtered.getPooledPileupA().getBaseCount()[variant];
		DefaultPileup[] filteredPileups1 = applyFilter(variant, filtered.getPileupsA());

		int count2 = filtered.getPooledPileupB().getBaseCount()[variant];
		DefaultPileup[] filteredPileups2 = applyFilter(variant, filtered.getPileupsB());

		// if nothing was filtered
		if(count1 == 0 && count2 == 0) {
			return true;
		}
		
		int filteredCount = 0;
		int count = 0;
		if(count1 > 0) {
			filtered.setPileupsA(filteredPileups1);
			filteredCount = filtered.getPooledPileupA().getBaseCount()[variant];
			count = count1;
		}
		if(count2 > 0) {
			filtered.setPileupsB(filteredPileups2);
			filteredCount = filtered.getPooledPileupB().getBaseCount()[variant];
			count = count2;
		}

		// FIXME if 2 RDDs remain don't filter or something
		if((double)filteredCount / (double)count <= 0.5) {
			return true;
		}

		filtered = parallelPileup;
		return false;
	}

	/**
	 * FIXME check this
	 * null if filter did not change anything
	 * @param extendedPileups
	 * @return
	 */
	private Pileup[] applyFilter(int variantBase, ExtendedPileup[] extendedPileups) {
		DefaultPileup[] filtered = new DefaultPileup[extendedPileups.length];

		boolean processed = false;
		for (int i = 0; i < extendedPileups.length; ++i) {
			ExtendedPileup extendedPileup = extendedPileups[i];
			if (extendedPileup.getFilteredCounts() == null) {
				filtered[i] = null;
			} else {
				filtered[i] = new DefaultPileup(extendedPileup.getPileup());
			
				filtered[i].getCounts().substract(variantBase, counts);
			}
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