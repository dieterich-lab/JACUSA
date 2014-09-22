package accusa2.method.call.statistic;

import accusa2.pileup.ParallelPileup;

public class DirichletMultinomialStatistic implements StatisticCalculator {

	public DirichletMultinomialStatistic() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean filter(double value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomialStatistic();
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
