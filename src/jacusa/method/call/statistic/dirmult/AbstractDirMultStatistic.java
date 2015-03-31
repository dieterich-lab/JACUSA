package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.util.MathUtil;

import java.util.Arrays;

import org.apache.commons.math3.special.Gamma;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public abstract class AbstractDirMultStatistic implements StatisticCalculator {

	// options for paremeters estimation
	protected int maxIterations = 100;
	protected double epsilon = 0.01; // TODO make smaller

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected Phred2Prob phred2Prob;

	protected boolean onlyObservedBases;

	protected double[] alpha1;
	protected double[] alpha2;
	protected double[] alphaP;
	
	public AbstractDirMultStatistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters = parameters;
		final int n = baseConfig.getBaseLength();
		this.baseConfig = baseConfig;
		phred2Prob = Phred2Prob.getInstance(n);
		onlyObservedBases = false;
	}

	protected abstract void populate(final Pileup[] pileups, final int[] baseIs, double[] alpha, double[] pileupCoverages, double[][] pileupMatrix);

	@Override
	public synchronized void addStatistic(Result result) {
		final double statistic = getStatistic(result.getParellelPileup());
		result.setStatistic(statistic);
		
		StringBuilder sb = new StringBuilder();
		sb.append("alpha1=");
		sb.append(Double.toString(alpha1[0]));
		for (int i = 1; i < alpha1.length; ++i) {
			sb.append(",");
			sb.append(Double.toString(alpha1[i]));
		}
		sb.append(";alpha2=");
		sb.append(Double.toString(alpha2[0]));
		for (int i = 1; i < alpha2.length; ++i) {
			sb.append(",");
			sb.append(Double.toString(alpha2[i]));
		}
		sb.append(";alphaP=");
		sb.append(Double.toString(alphaP[0]));
		for (int i = 1; i < alphaP.length; ++i) {
			sb.append(",");
			sb.append(Double.toString(alphaP[i]));
		}
		result.addInfo(sb.toString());
	}
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		final int baseIs[] = getBaseIs(parallelPileup);
		int baseN = baseConfig.getBaseLength();

		ChiSquareDist dist = new ChiSquareDist(baseN);

		alpha1 = new double[baseN];
		double[] pileupCoverages1 = new double[parallelPileup.getN1()];
		double[][] pileupMatrix1 = new double[parallelPileup.getN1()][baseN];

		alpha2 = new double[baseN];
		double[] pileupCoverages2 = new double[parallelPileup.getN2()];
		double[][] pileupMatrix2 = new double[parallelPileup.getN2()][baseN];

		alphaP = new double[baseN];
		double[] pileupCoveragesP = new double[parallelPileup.getN()];
		double[][] pileupMatrixP = new double[parallelPileup.getN()][baseN];

		populate(parallelPileup.getPileups1(), baseIs, alpha1, pileupCoverages1, pileupMatrix1);
		populate(parallelPileup.getPileups2(), baseIs, alpha2, pileupCoverages2, pileupMatrix2);
		populate(parallelPileup.getPileupsP(), baseIs, alphaP, pileupCoveragesP, pileupMatrixP);
		
		if (parallelPileup.getN1() == 1) {
			// System.arraycopy(alphaP, 0, alpha1, 0, alphaP.length);
		}
		
		if (parallelPileup.getN2() == 1) {
			// System.arraycopy(alphaP, 0, alpha2, 0, alphaP.length);
		}

		double p = -1.0;
		try {
			double logLikelihood1 = maximizeLogLikelihood(baseIs, alpha1, pileupCoverages1, pileupMatrix1);
			double logLikelihood2 = maximizeLogLikelihood(baseIs, alpha2, pileupCoverages2, pileupMatrix2);
			double logLikelihoodP = maximizeLogLikelihood(baseIs, alphaP, pileupCoveragesP, pileupMatrixP);
			// LRT
			double z = -2 * (logLikelihoodP - (logLikelihood1 + logLikelihood2));
			p = 1 - dist.cdf(z);
		} catch (StackOverflowError e) {
			System.out.println("Warning: Numerical Stability");
			System.out.println(parallelPileup.getContig());
			System.out.println(parallelPileup.getStart());
			System.out.println(parallelPileup.prettyPrint());
			return Double.MAX_VALUE;
		}

		return p;
	}

	/* TODO remove 
	protected void printAlpha(double[] alphas) {
		StringBuilder sb = new StringBuilder();
		for (double alpha : alphas) {
			sb.append(Double.toString(alpha));
			sb.append("\t");
		}
		System.out.println(sb.toString());
	}
	*/
	
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
			
			/*
			StringBuilder sb1 = new StringBuilder();
			sb1.append("g");
			StringBuilder sb2 = new StringBuilder();
			sb2.append("Q");
			StringBuilder sb3 = new StringBuilder();
			sb3.append("a1");
			StringBuilder sb4 = new StringBuilder();
			sb4.append("a2");
			*/
			
			// update alphaNew
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				if (alphaNew[baseI] < 0.0) {
					alphaNew[baseI] = 0.001;
				}
				
				/*
				sb1.append("\t" + gradient[baseI]);
				sb2.append("\t" + Q[baseI]);
				sb3.append("\t" + alphaOld[baseI]);
				sb4.append("\t" + alphaNew[baseI]);
				*/
			}
			/*
			System.out.println(iteration + "=====");
			System.out.println(sb3.toString());
			System.out.println(sb4.toString());
			System.out.println(sb1.toString());
			System.out.println("b\t" + b);
			System.out.println(sb2.toString());
			System.out.println("=====");
			*/

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
		return parameters.getThreshold() < value;
	}

	// format -u DirMult:epsilon=<epsilon>:maxIterations=<maxIterions>:onlyObserved
	@Override
	public void processCLI(String line) {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		for (int i = 1; i < s.length; ++i) {
			// key=value
			String[] kv = s[i].split("=");
			String key = kv[0];
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}

			// set value
			if (key.equals("epsilon")) {
				epsilon = Double.parseDouble(value);
			} else if(key.equals("maxIterations")) {
				maxIterations = Integer.parseInt(value);
			} else if(key.equals("onlyObserved")) {
				onlyObservedBases = true;
			} else {
				throw new IllegalArgumentException("Invalid argument " + key + " IN: " + line);
			}
		}
	}

	public int[] getBaseIs(ParallelPileup parallelPileup) {
		if (onlyObservedBases) {
			return parallelPileup.getPooledPileup().getAlleles();
		}

		return baseConfig.getBasesI();
	}
}