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

public class DirichletMultinomialMLEStatistic implements StatisticCalculator {

	// options for paremeters estimation
	protected final int maxIterations = 100;
	protected final double epsilon = 1.0/(double)(10^6);

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected Phred2Prob phred2Prob;
	
	public DirichletMultinomialMLEStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
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
		ChiSquareDist dist = new ChiSquareDist(parallelPileup.getNA() + parallelPileup.getNB() - 2);

		double[] alphaA = initialAlphaEstimate(baseIs, parallelPileup.getPileupsA());
		double logLikelihoodA = estimateAlpha(alphaA, baseIs, parallelPileup.getPileupsA());

		double[] alphaB = initialAlphaEstimate(baseIs, parallelPileup.getPileupsB());
		double logLikelihoodB = estimateAlpha(alphaB, baseIs, parallelPileup.getPileupsB());

		double[] alphaP = initialAlphaEstimate(baseIs, parallelPileup.getPileupsP());
		double logLikelihoodP = estimateAlpha(alphaP, baseIs, parallelPileup.getPileupsP());

		// LRT
		double z = -2 * (logLikelihoodP - (logLikelihoodA + logLikelihoodB));
		// z ~ chisquare
		return 1 - dist.cdf(z);
	}

	@Override
	public boolean filter(double value) {
		return parameters.getStat() < value;
	}

	@Override
	public StatisticCalculator newInstance() {
		return new DirichletMultinomialMLEStatistic(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial";
	}

	// TODO make better estimation -> converges faster
	protected double[] initialAlphaEstimate(int[] baseIs, Pileup[] pileups) {
		// actual values
		double[] alpha = new double[baseIs.length];
		Arrays.fill(alpha, 0.0);
		for (Pileup pileup : pileups) {
			double[] sum = phred2Prob.colSum(baseIs, pileup);
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alpha[baseI] += sum[baseI];
			}
		}
		for (int baseI = 0; baseI < baseIs.length; ++baseI) {
			alpha[baseI] /= (double)pileups.length;
		}

		return alpha;
	}

	// estimate alpha and returns loglik
	protected double estimateAlpha(double[] alphaOld, int[] baseIs, Pileup[] pileups) {
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
		
		// pileup related counts/containters/with prior knowledge
		double[][] nIK = new double[pileups.length][baseIs.length];
		double[] nI = new double[pileups.length];
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			nI[pileupI] = (double)pileups[pileupI].getCoverage();
			nIK[pileupI] = phred2Prob.colSum(baseIs, pileups[pileupI]);
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
				// reset
				gradient[baseI] = 0.0;
				Q[baseI] = 0.0;

				for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
					// calculate gradient
					gradient[baseI] += digammaSummedAlphaOld;
					gradient[baseI] -= digamma(nI[pileupI] + summedAlphaOld);

					gradient[baseI] += digamma(nIK[pileupI][baseI] + alphaOld[baseI]);
					gradient[baseI] -= digamma(alphaOld[baseI]);

					// calculate Q
					Q[baseI] += trigamma(nIK[pileupI][baseI] + alphaOld[baseI]);
					Q[baseI] -= trigamma(alphaOld[baseI]);
				}

				// calculate b
				b += gradient[baseI] / Q[baseI];
				b_DenominatorSum += 1.0 / Q[baseI];
			}

			// calculate z
			z = 0.0;
			for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
				z += trigammaSummedAlphaOld;
				z -= trigamma(nI[pileupI] + summedAlphaOld);
			}
			// calculate b cont.
			b = b / (1.0 / z + b_DenominatorSum);
			
			loglikOld = getLogLikelihood(alphaOld, baseIs, pileups);
			// update alphaNew
			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				// check that alpha is not < 0
				if (alphaNew[baseI] < 0) {
					alphaNew[baseI] = 0.005; // hard set
				}
			}
			loglikNew = getLogLikelihood(alphaNew, baseIs, pileups);
			// update value
			alphaOld = alphaNew.clone();

			// check if converged
			double delta = Math.abs(loglikNew - loglikOld);
			if (delta  <= epsilon) {
				converged = true;
			}
			iteration++;
		}

		return loglikNew;
	}

	// calculate likelihood
	public double getLogLikelihood(double[] alpha, int[] baseIs, Pileup[] pileups) {
		double logLikelihood = 0.0;
		double alphaSum = MathUtil.sum(alpha);

		for (Pileup pileup : pileups) {
			double nI = (double)pileup.getCoverage() ;
			double[] nIK = phred2Prob.colSum(baseIs, pileup);

			logLikelihood += Gamma.logGamma(alphaSum);
			logLikelihood -= Gamma.logGamma(nI + alphaSum);

			for (int baseI = 0; baseI < baseIs.length; ++baseI) {
				logLikelihood += Gamma.logGamma(nIK[baseI] + alpha[baseI]);
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
	
}