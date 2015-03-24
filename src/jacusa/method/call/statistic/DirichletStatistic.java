package jacusa.method.call.statistic;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public class DirichletStatistic implements StatisticCalculator {

	// options for parameters estimation
	private int maxIterations = 100;
	private double epsilon = 0.001;
	
	@SuppressWarnings("unused")
	private double estimatedError = 0.01; // TODO make use of error prob

	private final StatisticParameters parameters;
	private final BaseConfig baseConfig;
	private Phred2Prob phred2Prob;
	
	public DirichletStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;
		int n = baseConfig.getBases().length; 
		this.baseConfig = baseConfig;
		phred2Prob = Phred2Prob.getInstance(n);
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		final int baseIs[] = baseConfig.getBasesI();

		ChiSquareDist dist = new ChiSquareDist(4);

		double logLikelihood1 = maximizeLogLikelihood(baseIs, parallelPileup.getPileups1());
		double logLikelihood2 = maximizeLogLikelihood(baseIs, parallelPileup.getPileups2());
		double logLikelihoodP = maximizeLogLikelihood(baseIs, parallelPileup.getPileupsP());

		// LRT
		double z = -2 * (logLikelihoodP - (logLikelihood1 + logLikelihood2));

		// z ~ chisquare
		return 1 - dist.cdf(z);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getMaxStat() < value;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletStatistic(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "Dir";
	}

	@Override
	public String getDescription() {
		return "Dirichlet - Only Phred score";
	}


	// estimate alpha and returns loglik
	protected double maximizeLogLikelihood(int[] baseIs, Pileup[] pileups) {
		// parameters
		int iteration = 0;
		boolean converged = false;

		double[] alphaNew = new double[baseConfig.getBases().length];
		Arrays.fill(alphaNew, 0.0);

		// container 
		double[] gradient = new double[baseConfig.getBases().length];
		double[] Q = new double[baseConfig.getBases().length];
		double b;
		double z;
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;
		double loglikOld = Double.NEGATIVE_INFINITY;;
		double loglikNew = Double.NEGATIVE_INFINITY;

		// pileup related counts/containters/with prior knowledge
		double[] logProbMean = new double[baseConfig.getBases().length];
		double[] probMean = new double[baseConfig.getBases().length];
		// initial estimate for alpha. estimated by MOM
		double[] alphaOld = new double[baseConfig.getBases().length];
		Arrays.fill(alphaOld, 0.0);
		int N = pileups.length;

		for (int pileupI = 0; pileupI < N; ++pileupI) {
			double[] pileupMean = phred2Prob.colMeanProb(baseIs, pileups[pileupI]);
			double[] pileupSum = phred2Prob.colSumProb(baseIs, pileups[pileupI]);
			for (int baseI : baseIs) {
				alphaOld[baseI] += pileupSum[baseI];
				logProbMean[baseI] += Math.log(pileupMean[baseI]);
				probMean[baseI] += pileupMean[baseI];
			}
		}
		for (int baseI : baseIs) {
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
			for (int baseI : baseIs) {
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
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				// check that alpha is not < 0
				if (alphaNew[baseI] < 0) {
					alphaNew[baseI] = 0.005; // hard set
				}
			}
			loglikNew = getLogLikelihood(alphaNew, baseIs, N, logProbMean);
			

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
			if (delta  <= epsilon) {
				converged = true;
			}
			// update value
			System.arraycopy(alphaNew, 0, alphaOld, 0, baseConfig.getBases().length);

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
		for (int baseI : baseIs) {
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
	
	public void processCLI(String line) {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format -u Dir:epsilon:maxIterations:estimatedError
		for (int i = 1; i < s.length; ++i) {
			

			switch(i) {

			case 1:
				epsilon = Double.parseDouble(s[i]);
				break;

			case 2:
				maxIterations = Integer.parseInt(s[i]);
				break;

			case 3:
				estimatedError = Double.parseDouble(s[i]);
				break;
				
			default:
				throw new IllegalArgumentException("Invalid argument " + line);
			}
		}
	}

}