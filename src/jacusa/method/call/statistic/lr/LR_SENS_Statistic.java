package jacusa.method.call.statistic.lr;


import jacusa.cli.parameters.StatisticParameters;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;

/**
 * 
 * @author Michael Piechotta
 * Calculation of the parameters are based upon DefaultStatistic.
 * Likelihood ratio test to test whether "two dirichlet distributions are similar"
 * Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )
 * Use effective coverage for the calculation of alphas.
 * -> lower specificity, higher sensitivity
 */

public final class LR_SENS_Statistic extends AbstractLRStatistic {
	
	public LR_SENS_Statistic(BaseConfig baseConfig, StatisticParameters parameters) {
		super("lr-sens", "likelihood ratio test (effective coverage for alpha). Z = -2 ln( ( Dir(p,1) Dir(p,2) ) / ( Dir(1,1) Dir(2,2) ) )", baseConfig, parameters);
	}

	@Override
	public StatisticCalculator newInstance() {
		return new LR_SENS_Statistic(baseConfig, parameters);
	}

	@Override
	protected int getCoverage1(ParallelPileup parallelPileup) {
		return estimateParameters.getMeanCoverage(parallelPileup.getPileups1());
	}

	@Override
	protected int getCoverage2(ParallelPileup parallelPileup) {
		return estimateParameters.getMeanCoverage(parallelPileup.getPileups2());
	}

	@Override
	protected int getCoverageP(ParallelPileup parallelPileup) {
		return estimateParameters.getMeanCoverage(parallelPileup.getPileupsP());
	}

	@Override
	public boolean processCLI(String line) {
		return false;
	}
	
}