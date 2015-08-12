package jacusa.method.call.statistic;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.MinkaEstimateParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.AlphaInitFactory;
import jacusa.method.call.statistic.dirmult.initalpha.BayesAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.RonningAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.RonningBayesAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.WeirAlphaInit;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

public abstract class AbstractDirichletStatistic implements StatisticCalculator {

	protected final StatisticParameters parameters;
	protected final BaseConfig baseConfig;
	protected Phred2Prob phred2Prob;

	protected boolean onlyObservedBases;
	protected boolean showAlpha;
	protected boolean calcPValue;
	
	protected double[] alpha1;
	protected double[] alpha2;
	protected double[] alphaP;

	protected double[] initAlpha1;
	protected double[] initAlpha2;
	protected double[] initAlphaP;
	
	protected int iterations1;
	protected int iterations2;
	protected int iterationsP;
	protected double logLikelihood1;
	protected double logLikelihood2;
	protected double logLikelihoodP;
	
	protected boolean numericallyStable;
	protected StringBuilder info1;
	protected StringBuilder info2;
	protected StringBuilder infoP;

	private AlphaInitFactory alphaInitFactory;
	
	protected MinkaEstimateParameters estimateAlpha;
	
	public AbstractDirichletStatistic(final MinkaEstimateParameters estimateAlpha, final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters 	= parameters;
		final int n 		= baseConfig.getBaseLength();
		this.baseConfig 	= baseConfig;
		phred2Prob 			= Phred2Prob.getInstance(n);
		onlyObservedBases 	= false;
		showAlpha			= false;

		this.estimateAlpha	= estimateAlpha;
		
		alphaInitFactory	= new AlphaInitFactory(getAlphaInits());
	}

	protected Map<String, AbstractAlphaInit> getAlphaInits() {
		Map<String, AbstractAlphaInit> alphaInits = new HashMap<String, AbstractAlphaInit>();

		AbstractAlphaInit alphaInit = new RonningAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		alphaInit = new BayesAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		alphaInit = new WeirAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		/* 
		 * combined alpha init(s)
		 * combined methods for 
		 * - replicates > 1, and 
		 * - no replicates
		 */
		alphaInit = new RonningBayesAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		alphaInit = new RonningBayesAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		return alphaInits;
	}
	
	/**
	 * 
	 * @param pileups
	 * @param baseIs
	 * @param pileupMatrix
	 */
	protected void populate(
			final Pileup[] pileups, 
			final int[] baseIs, 
			double[][] pileupMatrix) {
		double[] pileupErrorVector = new double[baseIs.length];
		
		for (int pileupI = 0; pileupI < pileups.length; ++pileupI) {
			Pileup pileup = pileups[pileupI];
	
			populate(pileup, baseIs, pileupErrorVector, pileupMatrix[pileupI]);
		}
	}

	/**
	 * 
	 * @param pileup
	 * @param baseIs
	 * @param pileupErrorVector
	 * @param pileupVector
	 */
	protected abstract void populate(
			final Pileup pileup, 
			final int[] baseIs, 
			double[] pileupErrorVector,
			double[] pileupVector);

	@Override
	public synchronized void addStatistic(Result result) {
		final double statistic = getStatistic(result.getParellelPileup());
		result.setStatistic(statistic);

		// append content to info field
		StringBuilder sb = new StringBuilder();
		if (! isNumericallyStable()) {
			sb.append("NumericallyInstable;");
		}
		if (getInfo1().length() > 0) {
			sb.append("1=");
			sb.append(getInfo1().toString());
			sb.append(";");
		}
		if (getInfo2().length() > 0) {
			sb.append("2=");
			sb.append(getInfo2().toString());
			sb.append(";");
		}
		if (getInfoP().length() > 0) {
			sb.append("P=");
			sb.append(getInfoP().toString());
			sb.append(";");
		}

		// append alpha/iterations/log-likelihood to info info field
		if (showAlpha) {
			DecimalFormat df = new DecimalFormat("0.00"); 
			sb.append("alpha1=");
			sb.append(df.format(alpha1[0]));
			for (int i = 1; i < alpha1.length; ++i) {
				sb.append(":");
				sb.append(df.format(alpha1[i]));
			}
			sb.append(";alpha2=");
			sb.append(df.format(alpha2[0]));
			for (int i = 1; i < alpha2.length; ++i) {
				sb.append(":");
				sb.append(df.format(alpha2[i]));
			}
			sb.append(";alphaP=");
			sb.append(df.format(alphaP[0]));
			for (int i = 1; i < alphaP.length; ++i) {
				sb.append(":");
				sb.append(df.format(alphaP[i]));
			}

			sb.append(";initAlpha1=");
			sb.append(df.format(initAlpha1[0]));
			for (int i = 1; i < initAlpha1.length; ++i) {
				sb.append(":");
				sb.append(df.format(initAlpha1[i]));
			}
			sb.append(";initAlpha2=");
			sb.append(df.format(initAlpha2[0]));
			for (int i = 1; i < initAlpha2.length; ++i) {
				sb.append(":");
				sb.append(df.format(initAlpha2[i]));
			}
			sb.append(";initAlphaP=");
			sb.append(df.format(initAlphaP[0]));
			for (int i = 1; i < initAlphaP.length; ++i) {
				sb.append(":");
				sb.append(df.format(initAlphaP[i]));
			}
			
			sb.append(";");
			sb.append("logLikelihood1=");
			sb.append(logLikelihood1);
			sb.append(";");
			sb.append("logLikelihood2=");
			sb.append(logLikelihood2);
			sb.append(";");
			sb.append("logLikelihoodP=");
			sb.append(logLikelihoodP);
			sb.append(";");
			sb.append("iterations1=");
			sb.append(iterations1);
			sb.append(";");
			sb.append("iterations2=");
			sb.append(iterations2);
			sb.append(";");
			sb.append("iterationsP=");
			sb.append(iterationsP);
			sb.append(";");
		}

		// append to info field
		result.addInfo(sb.toString());
	}
	
	public double[] getAlpha1() {
		return alpha1;
	}
	public double[] getAlpha2() {
		return alpha2;
	}
	public double[] getAlphaP() {
		return alphaP;
	}
	public double[] getInitAlpha1() {
		return initAlpha1;
	}
	public double[] getInitAlpha2() {
		return initAlpha2;
	}
	public double[] getInitAlphaP() {
		return initAlphaP;
	}
	public double getLogLikelihood1() {
		return logLikelihood1;
	}
	public double getLogLikelihood2() {
		return logLikelihood2;
	}
	public double getLogLikelihoodP() {
		return logLikelihoodP;
	}
	public StringBuilder getInfo1() {
		return info1;
	}
	public StringBuilder getInfo2() {
		return info2;
	}
	public StringBuilder getInfoP() {
		return infoP;
	}
	public boolean isNumericallyStable() {
		return numericallyStable;
	}
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		// base index mask; can be ACGT or only observed bases in parallelPileup
		final int baseIs[] = getBaseIs(parallelPileup);
		// number of globally considered bases, normally 4 : ACGT
		int baseN = baseConfig.getBaseLength();

		// flag to indicated numerical stability of parameter estimation
		numericallyStable = true;

		// parameters for distribution
		alpha1 = new double[baseN];
		// container[# of pileups][baseN] for data to be modeled by distribution
		double[][] pileupMatrix1  = new double[parallelPileup.getN1()][baseN];
		// info container for sample 1
		info1 = new StringBuilder();
		
		// the same for sample 2
		alpha2 = new double[baseN];
		double[][] pileupMatrix2 = new double[parallelPileup.getN2()][baseN];
		info2 = new StringBuilder();

		// the same for pooled sample 1, 2
		alphaP = new double[baseN];
		double[][] pileupMatrixP = new double[parallelPileup.getN()][baseN];
		infoP = new StringBuilder();
		
		/* distinguish parameters estimation between 
		 * - no replicates (pileup.length == 1),
		 * - and replicates > 1 (pileup.length > 1)
		 */ 
		if (parallelPileup.getPileups1().length == 1) {
			// container for pseudocounts
			final double[] pileupErrorVector1 = new double[baseN];
			// populate pileupMatrix with values to be modeled
			populate(parallelPileup.getPileups1()[0], baseIs, pileupErrorVector1, pileupMatrix1[0]);
			// perform an initial guess of alpha
			initAlpha1 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups1()[0], pileupMatrix1[0], pileupErrorVector1);
		} else {
			// populate pileupMatrix with values to be modeled
			populate(parallelPileup.getPileups1(), baseIs, pileupMatrix1);
			// perform an initial guess of alpha
			initAlpha1 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups1(), pileupMatrix1);
		}
		// store initial alpha guess
		System.arraycopy(initAlpha1, 0, alpha1, 0, baseN);

		// the same for sample 2
		if (parallelPileup.getPileups2().length == 1) {
			final double[] pileupErrorVector2 = new double[baseN];
			populate(parallelPileup.getPileups2()[0], baseIs, pileupErrorVector2, pileupMatrix2[0]);
			initAlpha2 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups2()[0], pileupMatrix2[0], pileupErrorVector2);
		} else {
			populate(parallelPileup.getPileups2(), baseIs, pileupMatrix2);
			initAlpha2 = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileups2(), pileupMatrix2);
		}
		System.arraycopy(initAlpha2, 0, alpha2, 0, baseN);
		
		// the same for pooled sample (pileup.length > 1 always true)
		populate(parallelPileup.getPileupsP(), baseIs, pileupMatrixP);
		initAlphaP = estimateAlpha.getAlphaInit().init(baseIs, parallelPileup.getPileupsP(), pileupMatrixP);
		System.arraycopy(initAlphaP, 0, alphaP, 0, baseN);

		// container for test-statistic
		double stat = Double.NaN;
		try {
			// estimate alphas, capture and info(s), and store log-likelihood
			logLikelihood1 = estimateAlpha.maximizeLogLikelihood(baseIs, alpha1, pileupMatrix1, info1);
			iterations1 = estimateAlpha.getIterations();
			logLikelihood2 = estimateAlpha.maximizeLogLikelihood(baseIs, alpha2, pileupMatrix2, info2);
			iterations2 = estimateAlpha.getIterations();
			logLikelihoodP = estimateAlpha.maximizeLogLikelihood(baseIs, alphaP, pileupMatrixP, infoP);
			iterationsP = estimateAlpha.getIterations();

			// we want a value?
			if (calcPValue) {
				stat = -2 * (logLikelihoodP - (logLikelihood1 + logLikelihood2));
				ChiSquareDist dist = new ChiSquareDist(baseIs.length - 1);
				stat = 1 - dist.cdf(stat);
			} else { // just the log-likelihood ratio
				stat = (logLikelihood1 + logLikelihood2) - logLikelihoodP;
			}
		} catch (StackOverflowError e) {
			// catch numerical instabilities and report
			numericallyStable = false;
			return stat;
		}

		return stat;
	}

	/**
	 * Pretty print alpha.
	 * Debug function.
	 * 
	 * @param alphas
	 */
	protected void printAlpha(double[] alphas) {
		StringBuilder sb = new StringBuilder();
		for (double alpha : alphas) {
			sb.append(Double.toString(alpha));
			sb.append("\t");
		}
		System.out.println(sb.toString());
	}

	@Override
	public boolean filter(double value) {
		// if p-value interpret threshold as upper bound
		if (calcPValue) {
			return parameters.getThreshold() < value;
		}
		
		// if log-likelihood ratio and value not set give all results
		if (parameters.getThreshold() == Double.NaN) {
			return false;
		}
		
		// if log-likelihood ratio interpret threshold as lower bound 
		return value < parameters.getThreshold();
	}

	
	@Override
	public boolean processCLI(String line) {
		// format: -u DirMult:epsilon=<epsilon>:maxIterations=<maxIterions>:onlyObserved
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// indicates if a CLI has been successfully parsed
		boolean r = false;

		// ignore any first array element of s (e.g.: s[0] = "-u DirMult") 
		for (int i = 1; i < s.length; ++i) {
			// kv := "key[=value]" 
			String[] kv = s[i].split("=");
			String key = kv[0];
			// value may be empty for options without arguments, e.g.: "onlyObserved"
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}

			// parse key and do something
			if (key.equals("epsilon")) { 
				estimateAlpha.setEpsilon(Double.parseDouble(value));
				r = true;
			} else if(key.equals("maxIterations")) {
				estimateAlpha.setMaxIterations(Integer.parseInt(value));
				r = true;
			} else if(key.equals("onlyObserved")) {
				onlyObservedBases = true;
				r = true;
			} else if(key.equals("calculateP-value")) {
				calcPValue = true;
				r = true;
			} else if(key.equals("showAlpha")) {
				showAlpha = true;
				r = true;
			} else if(key.equals("initAlpha")) {
				// parse arguments by factory
				AbstractAlphaInit alphaInit = alphaInitFactory.processCLI(value);
				estimateAlpha.setAlphaInit(alphaInit);
				r = true;
			}
		}

		return r;
	}

	/**
	 * 
	 * @param parallelPileup
	 * @return
	 */
	public int[] getBaseIs(ParallelPileup parallelPileup) {
		if (onlyObservedBases) {
			return parallelPileup.getPooledPileup().getAlleles();
		}

		return baseConfig.getBasesI();
	}
	
	public MinkaEstimateParameters getEstimateAlpha() {
		return  estimateAlpha;
	}

	/**
	 * Pretty prints pileups.
	 * Debug function.
	 * 
	 * @param pileups
	 * @return
	 */
	public String printPileups(Pileup[] pileups) {
		StringBuilder sb = new StringBuilder();
		
		// output sample: Ax,Cx,Gx,Tx
		for (Pileup pileup : pileups) {
			sb.append("\t");

			int i = 0;
			char b = BaseConfig.VALID[i];
			int baseI = baseConfig.getBaseI((byte)b);
			int count = 0;
			if (baseI >= 0) {
				count = pileup.getCounts().getBaseCount(baseI);
			}
			sb.append(count);
			++i;
			for (; i < BaseConfig.VALID.length; ++i) {
				b = BaseConfig.VALID[i];
				baseI = baseConfig.getBaseI((byte)b);
				count = 0;
				if (baseI >= 0) {
					count = pileup.getCounts().getBaseCount(baseI);
				}
				sb.append(",");
				sb.append(count);
			}
		}

		return sb.toString();
	}

	public void setShowAlpha(boolean showAlpha) {
		this.showAlpha = showAlpha;
	}

	public void setOnlyObservedBases(boolean onlyObservedBases) {
		this.onlyObservedBases = onlyObservedBases;
	}
	
}
