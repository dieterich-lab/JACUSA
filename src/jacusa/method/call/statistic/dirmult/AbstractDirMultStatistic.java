package jacusa.method.call.statistic.dirmult;


import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.util.MathUtil;

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public abstract class AbstractDirMultStatistic implements StatisticCalculator {

	// options for paremeters estimation
	protected int maxIterations = 100;
	protected double epsilon = 0.001;

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected Phred2Prob phred2Prob;

	private boolean filterBases;
	
	public AbstractDirMultStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;
		final int n = baseConfig.getBases().length; 
		this.baseConfig = baseConfig;
		phred2Prob = Phred2Prob.getInstance(n);
		filterBases = false;
	}

	protected abstract void populate(final Pileup[] pileups, final int[] baseIs, double[] alpha, double[] pileupCoverages, double[][] pileupMatrix);
	
	@Deprecated
	protected boolean isFilterBases() {
		return filterBases;
	}

	@Deprecated
	protected void setFilterBases(final boolean filterBases) {
		this.filterBases = filterBases;
	}

	@Override
	public double getStatistic(ParallelPileup parallelPileup) {
		final int baseIs[] = {0, 1, 2, 3};
		// final int baseIs[] = parallelPileup.getPooledPileup().getAlleles();

		ChiSquareDist dist = new ChiSquareDist(baseIs.length);
		int baseN = baseIs.length;

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
			System.out.println("Error: Numerical Stability");
			System.out.println(parallelPileup.getContig());
			System.out.println(parallelPileup.getPosition());
			System.out.println(parallelPileup.prettyPrint());
			return -1.0;
		}

		return p;
	}
	
	// estimate alpha and returns loglik
	protected double maximizeLogLikelihood(int[] baseIs, double[] alphaOld, double coverages[], double[][] matrix) {
		// optim "bounds"
		int iteration = 0;
		boolean converged = false;

		final int baseN = baseConfig.getBases().length;

		// init alpha new
		double[] alphaNew = new double[baseN];
		Arrays.fill(alphaNew, 0.0);

		// container see Minka
		double[] gradient = new double[baseN];
		double[] Q = new double[baseN];
		double b;
		double z;
		// holds pre-computed value
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;
		// log-likelihood
		double loglikOld = Double.NEGATIVE_INFINITY;;
		double loglikNew = Double.NEGATIVE_INFINITY;

		int pileupN = coverages.length;
		
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
				// reset
				gradient[baseI] = 0.0;
				Q[baseI] = 0.0;

				for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
					// calculate gradient
					gradient[baseI] += digammaSummedAlphaOld;
					gradient[baseI] -= digamma(coverages[pileupI] + summedAlphaOld);

					gradient[baseI] += digamma(matrix[pileupI][baseI] + alphaOld[baseI]);
					gradient[baseI] -= digamma(alphaOld[baseI]);

					// calculate Q
					Q[baseI] += trigamma(matrix[pileupI][baseI] + alphaOld[baseI]);
					Q[baseI] -= trigamma(alphaOld[baseI]);
				}

				// calculate b
				b += gradient[baseI] / Q[baseI];
				b_DenominatorSum += 1.0 / Q[baseI];
			}

			// calculate z
			z = 0.0;
			for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
				z += trigammaSummedAlphaOld;
				z -= trigamma(coverages[pileupI] + summedAlphaOld);
			}
			// calculate b cont.
			b = b / (1.0 / z + b_DenominatorSum);

			loglikOld = getLogLikelihood(alphaOld, baseIs, coverages, matrix);
			// update alphaNew
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				if (alphaNew[baseI] < 0.0) {
					alphaNew[baseI] = 0.001;
				}
			}
			loglikNew = getLogLikelihood(alphaNew, baseIs, coverages, matrix);

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
			if (delta  <= epsilon) {
				converged = true;
			}
			// update value
			System.arraycopy(alphaNew, 0, alphaOld, 0, alphaNew.length);
			iteration++;
		}

		return loglikNew;
	}

	// calculate likelihood
	protected double getLogLikelihood(final double[] alpha, final int[] baseIs, final double coverages[], final double[][] pileupMatrix) {
		double logLikelihood = 0.0;
		double alphaSum = MathUtil.sum(alpha);

		for (int pileupI = 0; pileupI < coverages.length; pileupI++) {
			logLikelihood += Gamma.logGamma(alphaSum);
			logLikelihood -= Gamma.logGamma(coverages[pileupI] + alphaSum);

			for (int baseI : baseIs) {
				logLikelihood += Gamma.logGamma(pileupMatrix[pileupI][baseI] + alpha[baseI]);
				logLikelihood -= Gamma.logGamma(alpha[baseI]);
			}
		}
		return logLikelihood;
	}

	protected double digamma(double x) {
		return Gamma.digamma(x);
	}

	protected double trigamma(double x) {
		return Gamma.trigamma(x);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() < value;
	}

	public void processCLI(String line) {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format -u DirMult:epsilon:maxIterations
		for (int i = 1; i < s.length; ++i) {
			

			switch(i) {

			case 1:
				epsilon = Double.parseDouble(s[i]);
				break;

			case 2:
				maxIterations = Integer.parseInt(s[i]);
				break;
				
			default:
				throw new IllegalArgumentException("Invalid argument " + line);
			}
		}
	}
	
}
