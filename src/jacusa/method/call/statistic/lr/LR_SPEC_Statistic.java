package jacusa.method.call.statistic.lr;


import jacusa.cli.parameters.StatisticParameters;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;

/**
 * 
 * @author Michael Piechotta
 *
 * Calculation of the parameters are based upon DefaultStatistic.
 * Likelihood ratio test to test whether "two dirichlet distributions are similar"
 * Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )
 * Use minimal coverage for the calculation of alphas.
 * -> higher specificity, lower sensitivity
 *
 */

public final class LR_SPEC_Statistic extends AbstractLRStatistic {
	
	public LR_SPEC_Statistic(BaseConfig baseConfig, StatisticParameters parameters) {
		super("lr-spec", "likelihood ratio test (min. coverage for alpha). Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )", baseConfig, parameters);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new LR_SPEC_Statistic(baseConfig, parameters);
	}

	@Override
	protected int getCoverage1(final ParallelPileup parallelPileup) {
		return getCoverage(parallelPileup);
	}
	
	@Override
	protected int getCoverage2(final ParallelPileup parallelPileup) {
		return getCoverage(parallelPileup);
	}
	
	@Override
	protected int getCoverageP(final ParallelPileup parallelPileup) {
		return getCoverage(parallelPileup);
	}

	protected int getCoverage(final ParallelPileup parallelPileup) {
		final int coverage1 = estimateParameters.getMeanCoverage(parallelPileup.getPileups1());
		final int coverage2 = estimateParameters.getMeanCoverage(parallelPileup.getPileups2());

		return Math.min(coverage1, coverage2);
	}

	@Override
	public boolean processCLI(String line) {
		return false;
	}
	
}