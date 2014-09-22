package accusa2.method.call.statistic;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.estimate.AbstractEstimateParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;

public class DirichletMultinomialStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final AbstractEstimateParameters estimateParameters;
	protected final BaseConfig baseConfig;

	public DirichletMultinomialStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;
		estimateParameters = parameters.getEstimateParameters();
		this.baseConfig = baseConfig;
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() > value;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomialStatistic(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DM_MOM";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial";
	}

}
