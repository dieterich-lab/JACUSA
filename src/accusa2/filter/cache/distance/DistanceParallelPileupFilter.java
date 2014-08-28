package accusa2.filter.cache.distance;

import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractParallelPileupFilter;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.pileup.DefaultPileup.Counts;

/**
 * @author mpiechotta
 *
 */
// CHECKED
public class DistanceParallelPileupFilter extends AbstractParallelPileupFilter {

	private int filterDistance;
	private final int filterI; 

	public DistanceParallelPileupFilter(char c, int filterDistance, Parameters parameters) {
		super(c);
		this.filterDistance = filterDistance;
		filterI = parameters.getFilterConfig().c2i(c);
	}

	// FIXME what is a variant base - how should we filter?
	// what if AAAA VS GGGG - same number?
	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		int[] variants = parallelPileup.getVariantBases();

		
		if (variants.length == 0 || DefaultParallelPileup.isHoHo(parallelPileup)) {
			return false; // FIXME all other cases are currently ignored
		}
		char refBase = parallelPileup.getPooledPileup().getReferenceBase();
		
		int variantBaseI = variants[0]; 

		int count1 = parallelPileup.getPooledPileupA().getBaseCount()[variantBaseI];
		Pileup[] filteredPileups1 = applyFilter(variantBaseI, parallelPileup.getPileupsA(), parallelPileup.getFilterCountsA()[filterI]);

		int count2 = parallelPileup.getPooledPileupB().getBaseCount()[variantBaseI];
		Pileup[] filteredPileups2 = applyFilter(variantBaseI, parallelPileup.getPileupsB(), parallelPileup.getFilterCountsB()[filterI]);

		// if nothing was filtered
		if(count1 == 0 && count2 == 0) {
			return true;
		}

		
		filtered = new DefaultParallelPileup(parallelPileup);

		int filteredCount = 0;
		int count = 0;
		if(count1 > 0) {
			filtered.setPileupsA(filteredPileups1);
			filteredCount = filtered.getPooledPileupA().getBaseCount()[variantBaseI];
			count = count1;
		}
		if(count2 > 0) {
			filtered.setPileupsB(filteredPileups2);
			filteredCount = filtered.getPooledPileupB().getBaseCount()[variantBaseI];
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
	 * null if filter did not change anything
	 * @param extendedPileups
	 * @return
	 */
	private Pileup[] applyFilter(int variantBaseI, Pileup[] pileups, Counts[] counts) {
		Pileup[] filtered = new DefaultPileup[pileups.length];

		boolean processed = false;
		for(int i = 0; i < pileups.length; ++i) {
			filtered[i] = new DefaultPileup(pileups[i]);
			Counts count = counts[i];
			if(count != null) { 
				filtered[i].getCounts().substract(variantBaseI, count);
				processed = true;
			}
		}

		return processed ? filtered : null;
	}
	
	public int getDistance() {
		return filterDistance;
	}
	
}