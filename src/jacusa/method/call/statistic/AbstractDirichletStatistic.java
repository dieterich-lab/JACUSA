package jacusa.method.call.statistic;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.estimate.MinkaEstimateParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.dirmult.initalpha.AbstractAlphaInit;
//import jacusa.method.call.statistic.dirmult.initalpha.AlphaInitFactory;
//import jacusa.method.call.statistic.dirmult.initalpha.BayesAlphaInit;
//import jacusa.method.call.statistic.dirmult.initalpha.MeanAlphaInit;
import jacusa.method.call.statistic.dirmult.initalpha.MinAlphaInit;
//import jacusa.method.call.statistic.dirmult.initalpha.RonningAlphaInit;
//import jacusa.method.call.statistic.dirmult.initalpha.RonningBayesAlphaInit;
//import jacusa.method.call.statistic.dirmult.initalpha.WeirAlphaInit;
//import jacusa.method.call.statistic.dirmult.initalpha.WeirBayesAlphaInit;
import jacusa.phred2prob.Phred2Prob;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.util.Info;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
// import java.util.HashMap;
// import java.util.Map;

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
	protected Info estimateInfo;

	// private AlphaInitFactory alphaInitFactory;
	
	protected MinkaEstimateParameters estimateAlpha;
	
	protected AbstractAlphaInit fallbackAlphaInit;
	
	private DecimalFormat decimalFormat;
	
	public AbstractDirichletStatistic(final MinkaEstimateParameters estimateAlpha, final BaseConfig baseConfig, final StatisticParameters parameters) {
		this.parameters 	= parameters;
		final int n 		= baseConfig.getBaseLength();
		this.baseConfig 	= baseConfig;
		phred2Prob 			= Phred2Prob.getInstance(n);
		onlyObservedBases 	= false;
		showAlpha			= false;

		this.estimateAlpha	= estimateAlpha;
		fallbackAlphaInit	= new MinAlphaInit();
		
		// alphaInitFactory	= new AlphaInitFactory(getAlphaInits());
		
		DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols();
		otherSymbols.setDecimalSeparator('.');
		otherSymbols.setGroupingSeparator(',');
		decimalFormat = new DecimalFormat("#.##", otherSymbols);
	}

	/*
	protected Map<String, AbstractAlphaInit> getAlphaInits() {
		Map<String, AbstractAlphaInit> alphaInits = new HashMap<String, AbstractAlphaInit>();

		AbstractAlphaInit alphaInit = new RonningAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		alphaInit = new BayesAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		alphaInit = new WeirAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		alphaInit = new MeanAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);
		
		alphaInit = new MinAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);
		
		/* 
		 * combined alpha init(s)
		 * combined methods for 
		 * - replicates > 1, and 
		 * - no replicates
		 *
		alphaInit = new RonningBayesAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		alphaInit = new WeirBayesAlphaInit();
		alphaInits.put(alphaInit.getName(), alphaInit);

		return alphaInits;
	}
	*/
	
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
		double[] pileupErrorVector = new double[BaseConfig.VALID.length];
		
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

		final Info resultInfo = result.getResultInfo(); 

		// append content to info field
		if (! isNumericallyStable()) {
			resultInfo.add("NumericallyInstable");
		}

		if (! estimateInfo.isEmpty()) {
			resultInfo.addAll(estimateInfo);
		}
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
	public boolean isNumericallyStable() {
		return numericallyStable;
	}
	
	public double estimate(
			final String sample, 
			final int[] baseIs,
			double[] alpha, 
			double[] initAlphaValues, 
			final AbstractAlphaInit alphaInit, 
			final Pileup[] pileups,
			final boolean backtrack ) {
		// populate pileupMatrix with values to be modeled
		final double[][] matrix  = new double[pileups.length][alpha.length];
		// container for pseudocounts
		final double[] vector = new double[alpha.length];
		
		/* distinguish parameters estimation between 
		 * - no replicates (pileup.length == 1),
		 * - and replicates > 1 (pileup.length > 1)
		 */
		if (pileups.length == 1) {
			// populate pileupMatrix with values to be modeled
			populate(pileups[0], baseIs, vector, matrix[0]);
			// perform an initial guess of alpha
			initAlphaValues = alphaInit.init(
					baseIs, 
					pileups[0], 
					matrix[0], 
					vector);
		} else {
			// populate pileupMatrix with values to be modeled
			populate(pileups, baseIs, matrix);
			// perform an initial guess of alpha
			System.arraycopy(alphaInit.init(baseIs, pileups, matrix), 0, initAlphaValues, 0, alpha.length);
		}
		// store initial alpha guess
		System.arraycopy(initAlphaValues, 0, alpha, 0, alpha.length);

		// estimate alpha(s), capture and info(s), and store log-likelihood
		return estimateAlpha.maximizeLogLikelihood(baseIs, alpha, matrix, sample, estimateInfo, backtrack);
	}
	
	@Override
	public double getStatistic(final ParallelPileup parallelPileup) {
		// base index mask; can be ACGT or only observed bases in parallelPileup
		final int baseIs[] = getBaseIs(parallelPileup);
		// number of globally considered bases, normally 4 : ACGT
		int baseN = baseConfig.getBaseLength();

		// flag to indicated numerical stability of parameter estimation
		numericallyStable = true;
		estimateInfo = new Info();

		// parameters for distribution
		alpha1 = new double[baseN];
		initAlpha1 = new double[baseN];
		// the same for sample 2
		alpha2 = new double[baseN];
		initAlpha2 = new double[baseN];
		// the same for pooled sample 1, 2
		alphaP = new double[baseN];
		initAlphaP = new double[baseN];

		// estimate alpha(s), capture and info(s), and store log-likelihood
		boolean isReset = false;
		logLikelihood1 = estimate("1", baseIs, alpha1, initAlpha1, estimateAlpha.getAlphaInit(), parallelPileup.getPileups1(), false);
		iterations1 = estimateAlpha.getIterations();
		isReset |= estimateAlpha.isReset();
		logLikelihood2 = estimate("2", baseIs, alpha2, initAlpha2, estimateAlpha.getAlphaInit(), parallelPileup.getPileups2(), false);
		iterations2 = estimateAlpha.getIterations();
		isReset |= estimateAlpha.isReset();
		logLikelihoodP = estimate("P", baseIs, alphaP, initAlphaP, estimateAlpha.getAlphaInit(), parallelPileup.getPileupsP(), false);
		iterationsP = estimateAlpha.getIterations();
		isReset |= estimateAlpha.isReset();

		if (isReset) {
			logLikelihood1 = estimate("1", baseIs, alpha1, initAlpha1, fallbackAlphaInit, parallelPileup.getPileups1(), true);
			iterations1 = estimateAlpha.getIterations();
			logLikelihood2 = estimate("2", baseIs, alpha2, initAlpha2, fallbackAlphaInit, parallelPileup.getPileups2(), true);
			iterations2 = estimateAlpha.getIterations();
			logLikelihoodP = estimate("P", baseIs, alphaP, initAlphaP, fallbackAlphaInit, parallelPileup.getPileupsP(), true);
			iterationsP = estimateAlpha.getIterations();
		}

		// container for test-statistic
		double stat = Double.NaN;
		try {

			// append alpha/iterations/log-likelihood to info info field
			if (showAlpha) {
				estimateInfo.add("alpha1", decimalFormat.format(alpha1[0]));			
				for (int i = 1; i < alpha1.length; ++i) {
					estimateInfo.add("alpha1", ":");
					estimateInfo.add("alpha1", decimalFormat.format(alpha1[i]));
				}
				estimateInfo.add("alpha2", decimalFormat.format(alpha2[0]));			
				for (int i = 1; i < alpha2.length; ++i) {
					estimateInfo.add("alpha2", ":");
					estimateInfo.add("alpha2", decimalFormat.format(alpha2[i]));
				}
				estimateInfo.add("alphaP", decimalFormat.format(alphaP[0]));			
				for (int i = 1; i < alphaP.length; ++i) {
					estimateInfo.add("alphaP", ":");
					estimateInfo.add("alphaP", decimalFormat.format(alphaP[i]));
				}
				
				estimateInfo.add("initAlpha1", decimalFormat.format(initAlpha1[0]));			
				for (int i = 1; i < initAlpha1.length; ++i) {
					estimateInfo.add("initAlpha1", ":");
					estimateInfo.add("initAlpha1", decimalFormat.format(initAlpha1[i]));
				}
				estimateInfo.add("initAlpha2", decimalFormat.format(initAlpha2[0]));			
				for (int i = 1; i < initAlpha2.length; ++i) {
					estimateInfo.add("initAlpha2", ":");
					estimateInfo.add("initAlpha2", decimalFormat.format(initAlpha2[i]));
				}
				estimateInfo.add("initAlphaP", decimalFormat.format(initAlphaP[0]));			
				for (int i = 1; i < initAlphaP.length; ++i) {
					estimateInfo.add("initAlphaP", ":");
					estimateInfo.add("initAlphaP", decimalFormat.format(initAlphaP[i]));
				}
				
				estimateInfo.add("iterations1", Integer.toString(iterations1));
				estimateInfo.add("iterations2", Integer.toString(iterations2));
				estimateInfo.add("iterationsP", Integer.toString(iterationsP));
				
				estimateInfo.add("logLikelihood1", Double.toString(logLikelihood1));
				estimateInfo.add("logLikelihood2", Double.toString(logLikelihood2));
				estimateInfo.add("logLikelihoodP", Double.toString(logLikelihoodP));
			}
			
			// we want a p-value?
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
			} /*else if(key.equals("initAlpha")) {
				// parse arguments by factory
				AbstractAlphaInit alphaInit = alphaInitFactory.processCLI(value);
				estimateAlpha.setAlphaInit(alphaInit);
				r = true;
				
			}*/
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
