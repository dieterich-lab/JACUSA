package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import umontreal.iro.lecuyer.probdist.ChiSquareDist;

// CHECKED
public abstract class AbstractDirMult extends AbstractDirMultStatistic {

	public AbstractDirMult(BaseConfig baseConfig, StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		final int baseIs[] = {0, 1, 2, 3};
		// final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		ChiSquareDist dist = new ChiSquareDist(baseIs.length);

		int baseN = BaseConfig.VALID.length;

		double[] alpha1 = new double[baseN];
		double[] pileupCoverages1 = new double[parallelPileup.getN1()];
		double[][] pileupMatrix1 = new double[parallelPileup.getN1()][baseIs.length];

		double[] alpha2 = new double[baseN];
		double[] pileupCoverages2 = new double[parallelPileup.getN2()];
		double[][] pileupMatrix2 = new double[parallelPileup.getN2()][baseIs.length];

		double[] alphaP = new double[baseN];
		double[] pileupCoveragesP = new double[parallelPileup.getN()];
		double[][] pileupMatrixP = new double[parallelPileup.getN()][baseIs.length];

		populate(parallelPileup.getPileups1(), baseIs, alpha1, pileupCoverages1, pileupMatrix1);
		populate(parallelPileup.getPileups2(), baseIs, alpha2, pileupCoverages2, pileupMatrix2);
		populate(parallelPileup.getPileupsP(), baseIs, alphaP, pileupCoveragesP, pileupMatrixP);

		double p = -1.0;
		try {
			double logLikelihood1 = maximizeLogLikelihood(baseIs, alpha1, pileupCoverages1, pileupMatrix1);
			double logLikelihood2 = maximizeLogLikelihood(baseIs, alpha2, pileupCoverages2, pileupMatrix2);
			double logLikelihoodP = maximizeLogLikelihood(baseIs, alphaP, pileupCoveragesP, pileupMatrixP);
			// LRT
			double z = -2 * (logLikelihoodP - (logLikelihood1 + logLikelihood2));

			p = 1 - dist.cdf(z);
		} catch (StackOverflowError e) {
			System.out.println("Error");
			System.out.println(parallelPileup.getContig());
			System.out.println(parallelPileup.getPosition());
			System.out.println(parallelPileup.prettyPrint());
			return -1.0;
		}

		return p;
	}

	protected abstract void populate(final Pileup[] pileups, final int[] baseIs, double[] alpha, double[] pileupCoverages, double[][] pileupMatrix);

}