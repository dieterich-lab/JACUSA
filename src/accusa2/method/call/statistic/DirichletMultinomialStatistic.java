package accusa2.method.call.statistic;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.estimate.DirichletMultinomialEstimation;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.process.phred2prob.Phred2Prob;

public class DirichletMultinomialStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final DirichletMultinomialEstimation estimateParameters;
	protected final BaseConfig baseConfig;

	public DirichletMultinomialStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;
		int n = baseConfig.getBases().length; 
		estimateParameters = new DirichletMultinomialEstimation(Phred2Prob.getInstance(n));
		this.baseConfig = baseConfig;
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		final int baseIs[] = {0, 1, 2, 3};
		//final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		ChiSquareDist dist = new ChiSquareDist(parallelPileup.getNA() + parallelPileup.getNB() - 2);

		double[] alphaA = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsA());
		double logLikelihoodA = estimateParameters.getLogLikelihood(alphaA, baseIs, parallelPileup.getPileupsA());

		double[] alphaB = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsB());
		double logLikelihoodB = estimateParameters.getLogLikelihood(alphaB, baseIs, parallelPileup.getPileupsB());

		double[] alphaP = estimateParameters.estimateAlpha(baseIs, parallelPileup.getPileupsP());
		double logLikelihoodP = estimateParameters.getLogLikelihood(alphaP, baseIs, parallelPileup.getPileupsP());

		double z = -2 * (logLikelihoodP - (logLikelihoodA + logLikelihoodB));
		return 1 - dist.cdf(z);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() < value;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomialStatistic(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial";
	}

}
