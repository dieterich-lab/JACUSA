package jacusa.method.call.statistic.dirmult;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.MinkaEstimateDirMultParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.AbstractDirichletStatistic;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.Pileup;

public class DirichletMultinomialCompoundError extends AbstractDirichletStatistic {

	protected double estimatedError = 0.01;
	protected double priorError = 0d;

	public DirichletMultinomialCompoundError(final BaseConfig baseConfig, final StatisticParameters parameters) {
		// sorry for ugly, code call to super constructor must be first call
		super(new MinkaEstimateDirMultParameters(), baseConfig, parameters);
	}

	@Override
	public String getName() {
		return "DirMult-CE";
	}

	@Override
	public String getDescription() {
		return "Compound Err. (estimated err.{" + estimatedError + "} + phred score)";  
		//" (DirMult-CE:epsilon=<epsilon>:maxIterations=<maxIterations>:estimatedError=<estimatedError>)";
	}

	@Override
	public void populate(final Pileup[] pileups, final int[] baseIs, double[][] pileupMatrix) {
		double[] pileupErrorVector = new double[baseIs.length];
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			Pileup pileup = pileups[pileupI];
			populate(pileup, baseIs, pileupErrorVector, pileupMatrix[pileupI]);
		}
	}

	@Override
	protected void populate(final Pileup pileup, final int[] baseIs, double[] pileupErrorVector, double[] pileupMatrix) {
		double[] pileupCount = phred2Prob.colSumCount(baseIs, pileup);
		double[] pileupError = phred2Prob.colMeanErrorProb(baseIs, pileup);

		for (int baseI : baseIs) {
			pileupMatrix[baseI] += priorError;

			if (pileupCount[baseI] > 0.0) {
				pileupMatrix[baseI] += pileupCount[baseI];
				for (int baseI2 : baseIs) {
					if (baseI != baseI2) {
						double combinedError = (pileupError[baseI2] + estimatedError) * (double)pileupCount[baseI] / (double)(baseIs.length - 1);
						pileupMatrix[baseI2] += combinedError;
						pileupErrorVector[baseI2] = combinedError;
					} else {
						// pileupMatrix[pileupI][baseI2] -= (estimatedError) * (double)pileupCount[baseI];
					}
				}
			} else {
			
			}
		}
	}

	@Override
	public DirichletMultinomialCompoundError newInstance() {
		return new DirichletMultinomialCompoundError(baseConfig, parameters);
	}

	// format -u DirMult:epsilon=<epsilon>:maxIterations=<maxIterions>:estimatedError=<estimatedError>
	@Override
	public boolean processCLI(String line) {
		boolean r = super.processCLI(line);
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
			if (key.equals("estimatedError")) {
				estimatedError = Double.parseDouble(value);
				r = true;
			} else if (!r){
				throw new IllegalArgumentException("Invalid argument " + key + " in line: " + line);
			}
		}
		
		return r;
	}
	
	public void setEstimatedError(double estimatedError) {
		this.estimatedError = estimatedError;
	}
	
}