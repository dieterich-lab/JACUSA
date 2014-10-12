package accusa2.method.call.statistic;

import java.util.Arrays;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.Pileup;
import accusa2.util.MathUtil;

public class DirichletMultinomialMLE2Statistic extends DirichletMultinomialMLEStatistic {

	public DirichletMultinomialMLE2Statistic(final BaseConfig baseConfig, final StatisticParameters parameters) {
		super(baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult2";
	}

	@Override
	public String getDescription() {
		return "Dirichlet-Multinomial Test";
	}

	// estimate alpha and returns loglik
	protected double maximizeLogLikelihood(int[] baseIs, double[] alphaOld, Pileup[] pileups) {
		// optim "bounds"
		int iteration = 0;
		boolean converged = false;
		
		int baseN = baseConfig.getBases().length;

		// init alpha new
		double[] alphaNew = new double[baseN];
		Arrays.fill(alphaNew, 0.0);

		// container see Minka
		double[] gradient = new double[baseN];
		double[] Q = new double[baseN];
		double b;
		double z;
		// holds precomputed value
		double summedAlphaOld;
		double digammaSummedAlphaOld;
		double trigammaSummedAlphaOld;
		// loglikelihood
		double loglikOld = Double.NEGATIVE_INFINITY;;
		double loglikNew = Double.NEGATIVE_INFINITY;

		// TODO make better estimation -> converges faster
		// pileup related counts/containters/with prior knowledge
		double[][] nIK = new double[pileups.length][baseN];
		double[] nI = new double[pileups.length];
		// alphaOld. estimate by MOM
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			nIK[pileupI] = phred2Prob.colSum2(baseIs, pileups[pileupI]); // FIXME

			for (int baseI : baseIs) {
				nIK[pileupI][baseI] += 0.001 * (double)pileups[pileupI].getCoverage();

				alphaOld[baseI] += nIK[pileupI][baseI]; // make better
			}

			nI[pileupI] = MathUtil.sum(nIK[pileupI]);
		}
		for (int baseI : baseIs) {
			alphaOld[baseI] = alphaOld[baseI] / (double)pileups.length;
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

			loglikOld = getLogLikelihood(alphaOld, baseIs, nI, nIK);
			// update alphaNew
			for (int baseI : baseIs) {
				alphaNew[baseI] = alphaOld[baseI] - (gradient[baseI] - b) / Q[baseI];
				if (alphaNew[baseI] < 0.0) {
					// TODO
					alphaNew[baseI] = 0.001;
				}
			}
			loglikNew = getLogLikelihood(alphaNew, baseIs, nI, nIK);

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

}