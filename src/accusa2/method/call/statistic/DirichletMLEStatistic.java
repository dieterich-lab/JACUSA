package accusa2.method.call.statistic;

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import accusa2.cli.parameters.StatisticParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.MathUtil;

public class DirichletMLEStatistic implements StatisticCalculator {

	// options for paremeters estimation
	protected final int maxIterations = 100;
	protected final double epsilon = 0.0001;

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected Phred2Prob phred2Prob;
	
	public DirichletMLEStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;
		int n = baseConfig.getBases().length; 
		this.baseConfig = baseConfig;
		phred2Prob = Phred2Prob.getInstance(n);
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		final int baseIs[] = {0, 1, 2, 3};
		//final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		// TODO how many FGs
		ChiSquareDist dist = new ChiSquareDist(2 * (baseIs.length - 1));

		double logLikelihoodA = maximizeLogLikelihood(baseIs, parallelPileup.getPileupsA());
		double logLikelihoodB = maximizeLogLikelihood(baseIs, parallelPileup.getPileupsB());
		double logLikelihoodP = maximizeLogLikelihood(baseIs, parallelPileup.getPileupsP());

		// LRT
		double z = -2 * (logLikelihoodP - (logLikelihoodA + logLikelihoodB));
		if (z > 0.0) {
			int j = 1;
			j++;
		}

		// z ~ chisquare
		return 1 - dist.cdf(z);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() < value;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMLEStatistic(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "Dir";
	}

	@Override
	public String getDescription() {
		return "Dirichlet";
	}


	// estimate alpha and returns loglik
	protected double maximizeLogLikelihood(int[] baseIs, Pileup[] pileups) {
		// parameters
		int iteration = 0;
		boolean converged = false;

		double[] alphaNew = new double[baseIs.length];
		Arrays.fill(alphaNew, 0.0);

		// container 
		double[] gradient = new double[baseIs.length];
		double[] Q = new double[baseIs.length];
		double b;
		double z;
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;
		double loglikOld = Double.NEGATIVE_INFINITY;;
		double loglikNew = Double.NEGATIVE_INFINITY;

		// TODO make better estimation -> converges faster
		// pileup related counts/containters/with prior knowledge
		double[] logProbMean = new double[baseIs.length];
		double[] probMean = new double[baseIs.length];
		// initial estimate for alpha. estimated by MOM
		double[] alphaOld = new double[baseIs.length];
		Arrays.fill(alphaOld, 0.0);
		int N = pileups.length;

		for (int pileupI = 0; pileupI < N; ++pileupI) {
			double[] sum = phred2Prob.colSum(baseIs, pileups[pileupI]);

			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				sum[baseI] += 1.0 / (double)baseIs.length;
			}

			double s = MathUtil.sum(sum);
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alphaOld[baseI] += sum[baseI];
				logProbMean[baseI] += Math.log(sum[baseI] / s);
				probMean[baseI] += sum[baseI] / s;
			}
		}
		for (int baseI = 0; baseI < baseIs.length; ++baseI) {
			alphaOld[baseI] /= (double)N;
			logProbMean[baseI] /= (double)N;
			probMean[baseI] /= (double)N;
		}

		// maximize
		while (iteration < maxIterations && ! converged) {
			// pre-compute
			summedAlphaOld = MathUtil.sum(alphaOld);
			digammaSummedAlphaOld = digamma(summedAlphaOld);
			trigammaSummedAlphaOld = trigamma(summedAlphaOld);

			// reset
			b = 0.0;
			double b_DenominatorSum = 0.0;
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				// calculate gradient
				// reset
				gradient[baseI] = (double)N * digammaSummedAlphaOld;
				gradient[baseI] -= (double)N * digamma(alphaOld[baseI]);
				gradient[baseI] += (double)N * logProbMean[baseI];

				// calculate Q
				// reset
				Q[baseI] = -(double)N * trigamma(alphaOld[baseI]);

				// calculate b
				b += gradient[baseI] / Q[baseI];
				b_DenominatorSum += 1.0 / Q[baseI];
			}

			// calculate z
			z = (double)N * trigammaSummedAlphaOld;
			// calculate b cont.
			b = b / (1.0 / z + b_DenominatorSum);

			loglikOld = getLogLikelihood(alphaOld, baseIs, N, logProbMean);
			// update alphaNew
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				// check that alpha is not < 0
				if (alphaNew[baseI] < 0) {
					alphaNew[baseI] = 0.005; // hard set
				}
			}
			loglikNew = getLogLikelihood(alphaNew, baseIs, N, logProbMean);
			// update value
			alphaOld = alphaNew.clone();

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
System.out.println(loglikNew);			
			if (delta  <= epsilon) {
				converged = true;
			}
			iteration++;
		}

		return loglikNew;
	}

	// calculate likelihood
	public double getLogLikelihood(double[] alpha, int[] baseIs, int N, double[] logProbMean) {
		double alphaSum = MathUtil.sum(alpha);

		double logLikelihood = (double)N * Gamma.logGamma(alphaSum);
		double tmp = 0.0;
		double tmp2 = 0.0;
		for (int baseI = 0; baseI < baseIs.length; ++baseI) {
			tmp += Gamma.logGamma(alpha[baseI]);
			tmp2 += (alpha[baseI] - 1.0) * logProbMean[baseI];
		}
		logLikelihood -= (double)N * tmp;
		logLikelihood += (double)N * tmp2;

		return logLikelihood;
	}

	protected double digamma(double x) {
		return Gamma.digamma(x);
	}

	protected double trigamma(double x) {
		return Gamma.trigamma(x);
	}
	
}