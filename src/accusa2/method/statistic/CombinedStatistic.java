package accusa2.method.statistic;

import accusa2.pileup.ParallelPileup;

/**
 * 
 * @author michael
 *
 * Calculates test-statistic based on number of alleles per parallel pileup.
 * 
 */

public final class CombinedStatistic implements StatisticCalculator {

	protected final StatisticCalculator ho_he;
	protected final StatisticCalculator he_he;

	public CombinedStatistic() {
		ho_he = new DefaultStatistic();
		he_he = new PooledStatistic();
	}

	@Override
	public StatisticCalculator newInstance() {
		return new CombinedStatistic();
	}

	public double getStatistic(final ParallelPileup parallelPileup) {
		if(parallelPileup.getPooledPileup1().getAlleles().length > 1 && parallelPileup.getPooledPileup2().getAlleles().length > 1) { 
			return he_he.getStatistic(parallelPileup);
		} else {
			return ho_he.getStatistic(parallelPileup);
		}
	}

	@Override
	public String getDescription() {
		return "default(ho:he) + pooled(he:he)";
	}

	@Override
	public String getName() {
		return "combined";
	}

}