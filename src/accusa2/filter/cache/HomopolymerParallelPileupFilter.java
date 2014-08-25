package accusa2.filter.cache;

import accusa2.cli.Parameters;
import accusa2.pileup.DefaultParallelPileup;
import accusa2.pileup.DefaultPileup;
import accusa2.pileup.DefaultPileup.Counts;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;

/**
 * @author mpiechotta
 *
 */
public class HomopolymerParallelPileupFilter extends AbstractParallelPileupFilter {

	private int length;
	private int distance;

	private int filterI;
	
	public HomopolymerParallelPileupFilter(char c, int length, int distance, Parameters parameters) {
		super(c);
		this.length = length;
		this.distance = distance;

		filterI = parameters.getFilterConfig().c2i(c);
	}

	// FIXME what is a variant base - how should we filter?
	// what if AAAA VS GGGG - same number?
	@Override
	public boolean filter(ParallelPileup parallelPileup) {
		this.filtered = new DefaultParallelPileup(parallelPileup);

		int[] alleles = parallelPileup.getPooledPileup().getAlleles();

		// determine the least abundant variant
		for(int baseI : alleles) {
			int count = parallelPileup.getPooledPileup().getBaseCount()[baseI];
			Pileup[] filteredPileups1 = applyFilter(baseI, parallelPileup.getPileupsA(), parallelPileup.getFilterCountsA()[filterI]);
			Pileup[] filteredPileups2 = applyFilter(baseI, parallelPileup.getPileupsB(), parallelPileup.getFilterCountsA()[filterI]);

			// if nothing was filtered
			if(filteredPileups1 == null && filteredPileups2 == null) {
				continue;
			}
			filtered = new DefaultParallelPileup(filteredPileups1, filteredPileups2);
			int filteredCount = filtered.getPooledPileup().getBaseCount()[baseI];

			// TODO make this more quantitative
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

	public int getLength() {
		return length;
	}

	public int getDistance() {
		return distance;
	}

}
