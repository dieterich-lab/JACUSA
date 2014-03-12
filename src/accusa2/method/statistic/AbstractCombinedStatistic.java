package accusa2.method.statistic;

import accusa2.cli.Parameters;
import accusa2.pileup.ParallelPileup;

/**
 * 
 * @author michael
 *
 * Calculates test-statistic based on number of alleles per parallel pileup.
 * 
 */

public abstract class AbstractCombinedStatistic implements StatisticCalculator {

	protected final Parameters parameters;
	
	protected final StatisticCalculator ho_he;
	protected final StatisticCalculator he_he;

	protected final String name;
	protected final String description;
	
	public AbstractCombinedStatistic(Parameters parameters, StatisticCalculator ho_he, StatisticCalculator he_he, String name, String description) {
		this.parameters 	= parameters;
		
		this.ho_he			= ho_he;
		this.he_he 			= ho_he;

		this.name 			= name;
		this.description 	= description;
	}

	public final double getStatistic(final ParallelPileup parallelPileup) {
		if(parallelPileup.getPooledPileup1().getAlleles().length > 1 && parallelPileup.getPooledPileup2().getAlleles().length > 1) { 
			return he_he.getStatistic(parallelPileup);
		} else {
			return ho_he.getStatistic(parallelPileup);
		}
	}
	
	// "default(ho:he) + pooled(he:he)"
	@Override
	public final String getDescription() {
		return description;
	}

	// combined
	@Override
	public final String getName() {
		return name;
	}

}