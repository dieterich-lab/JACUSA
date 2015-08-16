package jacusa.estimate;

import java.util.Arrays;

import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;

import jacusa.util.Info;
import jacusa.util.MathUtil;

import org.apache.commons.math3.special.Gamma;

public class MinkaEstimateDirMultParameters extends MinkaEstimateParameters {

	private final static double EPSILON = 0.001;
	private final static int MAX_ITERATIONS = 100;
	
	private double[] tmpCoverages;
	
	public MinkaEstimateDirMultParameters() {
		super();
	}

	public MinkaEstimateDirMultParameters(final AbstractAlphaInit initialAlpha) {
		this(initialAlpha, MAX_ITERATIONS, EPSILON);
	}

	public MinkaEstimateDirMultParameters(
			final int maxIterations, 
			final double epsilon) {
		super(maxIterations, epsilon);
	}
	
	public MinkaEstimateDirMultParameters(
			final AbstractAlphaInit initialAlpha, 
			final int maxIterations, 
			final double epsilon) {
		super(initialAlpha, maxIterations, epsilon);
	}
	
	// estimate alpha and returns loglik
	public double maximizeLogLikelihood(
			final int[] baseIs, 
			final double[] alphaOld, 
			final double[][] pileupMatrix,
			final String sample,
			final Info estimateInfo) {
		iterations = 0;

		final double localCoverages[] = getCoverages(baseIs, pileupMatrix);
		
		boolean converged = false;

		// final int baseN = baseConfig.getBases().length;
		final int baseN = alphaOld.length;
		
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
		double loglikOld = Double.NEGATIVE_INFINITY;
		double loglikNew = Double.NEGATIVE_INFINITY;

		int pileupN = pileupMatrix.length;

		boolean backtrack = false;
		reset = false;
		int num = 0;
		
		// maximize
		while (iterations < maxIterations && ! converged) {
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

				// System.out.println("baseI: " + baseI);
				for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
					// calculate gradient
					gradient[baseI] += digammaSummedAlphaOld;
					gradient[baseI] -= digamma(localCoverages[pileupI] + summedAlphaOld);
					// 
					gradient[baseI] += digamma(pileupMatrix[pileupI][baseI] + alphaOld[baseI]);
					gradient[baseI] -= digamma(alphaOld[baseI]);

					// calculate Q
					Q[baseI] += trigamma(pileupMatrix[pileupI][baseI] + alphaOld[baseI]);
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
				z -= trigamma(localCoverages[pileupI] + summedAlphaOld);
			}
			// calculate b cont.
			b = b / (1.0 / z + b_DenominatorSum);

			loglikOld = getLogLikelihood(alphaOld, baseIs, pileupMatrix);
			
			// try update alphaNew
			boolean admissible = true; 		
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];

				if (alphaNew[baseI] < 0.0) {
					// System.err.println("Reset! " + iterations);
					admissible = false;
				}
			}
			// check if alpha negative
			if (! admissible) {
				if (backtrack) {
					estimateInfo.add("backtrack" + sample, ",");
				}
				estimateInfo.add("backtrack" + sample, Integer.toString(iterations));
				num++;
				backtrack = true;

				// try newton backtracking
				alphaNew = backtracking(alphaOld, baseIs, gradient, b, Q);

				if (alphaNew == null) {
					alphaNew = new double[baseN];
	
					// if backtracking did not work use Ronning1989 -> min_k X_ik 
					for (int baseI : baseIs) {		
						double min = Double.MAX_VALUE;
						for (int pileupI = 0; pileupI < pileupN; ++pileupI) {
							min = Math.min(min, pileupMatrix[pileupI][baseI] / localCoverages[pileupI]);
						}
						alphaNew[baseI] = min;
					}
					if (reset) {
						estimateInfo.add("reset" + sample, ",");
					}
					estimateInfo.add("reset" + sample, Integer.toString(iterations));
					reset = true;
				}
			} else {
	
				// calculate log-likelihood for new alpha(s)
				loglikNew = getLogLikelihood(alphaNew, baseIs, pileupMatrix);
	
				// check if converged
				double delta = Math.abs(loglikNew - loglikOld);
				if (delta  <= EPSILON) {
					converged = true;
				}
			}
			// update value
			System.arraycopy(alphaNew, 0, alphaOld, 0, alphaNew.length);
			iterations++;	

			/*
			System.out.print("iter: " + iterations + "\t=>\t");
			System.out.println(printAlpha(alphaNew));
			*/
		}

		// reset
		this.tmpCoverages = null;

		return loglikNew;
	}
	
	private double[] getCoverages(final int[] baseIs, final double[][] pileupMatrix) {
		if (tmpCoverages == null) {
			int pileupN = pileupMatrix.length;
			tmpCoverages = new double[pileupN];
			for (int pileupI = 0; pileupI < pileupN; pileupI++) {
				double sum = 0.0;
				for (int baseI : baseIs) {
					sum += pileupMatrix[pileupI][baseI];
				}
				tmpCoverages[pileupI] = sum;
			}
		}

		return tmpCoverages;
	}
	
	// calculate likelihood
	protected double getLogLikelihood(
			final double[] alpha, 
			final int[] baseIs, 
			final double[][] pileupMatrix) {
		double logLikelihood = 0.0;
		final double alphaSum = MathUtil.sum(alpha);
		final double[] coverages = getCoverages(baseIs, pileupMatrix);

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

}
