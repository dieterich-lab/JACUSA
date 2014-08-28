package accusa2.filter.cache.distance;

import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractParallelPileupFilter;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

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
		int variantBaseI = getVariantBaseI(parallelPileup); 

		int countA = parallelPileup.getPooledPileupA().getBaseCount()[variantBaseI];
		Pileup[] filteredPileups1 = applyFilter(variantBaseI, parallelPileup.getPileupsA(), parallelPileup.getFilterCountsA()[filterI]);

		int countB = parallelPileup.getPooledPileupB().getBaseCount()[variantBaseI];
		Pileup[] filteredPileups2 = applyFilter(variantBaseI, parallelPileup.getPileupsB(), parallelPileup.getFilterCountsB()[filterI]);

		// if nothing was filtered
		if(countA == 0 && countB == 0) {
			return true;
		}

		filtered = new DefaultParallelPileup(parallelPileup);

		int filteredCount = 0;
		int count = 0;
		if(countA > 0) {
			filtered.setPileupsA(filteredPileups1);
			filteredCount = filtered.getPooledPileupA().getBaseCount()[variantBaseI];
			count = countA;
		}
		if(countB > 0) {
			filtered.setPileupsB(filteredPileups2);
			filteredCount = filtered.getPooledPileupB().getBaseCount()[variantBaseI];
			count = countB;
		}

		// FIXME if 2 RDDs remain don't filter or something
		if((double)filteredCount / (double)count <= 0.5) {
			return true;
		}

		filtered = parallelPileup;
		return false;
	}

	
	public int getDistance() {
		return filterDistance;
	}
	
}