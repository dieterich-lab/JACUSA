package accusa2.method.call.statistic;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

/**
 * 
 * @author michael
 *
 * Calculation of the parameters are based upon DefaultStatistic.
 * Likelihood ratio test to test whether "two dirichlet distributions are similar"
 * Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )
 * Use minimal coverage for the calculation of alphas.
 * -> higher specificity, lower sensitivity
 *
 */

public final class LRStatistic extends AbstractLRStatistic {
	
	public LRStatistic(BaseConfig baseConfig, StatisticParameters parameters) {
		super("lr", "likelihood ratio test (min. coverage for alpha). Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )", baseConfig, parameters);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new LRStatistic(baseConfig, parameters);
	}

	@Override
	protected int getCoverageA(final ParallelPileup parallelPileup) {
		return getCoverage(parallelPileup);
	}
	
	@Override
	protected int getCoverageB(final ParallelPileup parallelPileup) {
		return getCoverage(parallelPileup);
	}
	
	@Override
	protected int getCoverageP(final ParallelPileup parallelPileup) {
		return getCoverage(parallelPileup);
	}

	protected int getCoverage(final ParallelPileup parallelPileup) {
		final int coverageA = estimateParameters.getMeanCoverage(parallelPileup.getPileupsA());
		final int coverageB = estimateParameters.getMeanCoverage(parallelPileup.getPileupsB());

		return Math.min(coverageA, coverageB);
	}

}