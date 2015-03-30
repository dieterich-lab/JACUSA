package jacusa.filter.storage.outlier;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.result.Result;
import jacusa.util.Location;

class MaxRangeOutlierFilter extends OutlierStorageFilter {

	private double threshold;
	private StatisticParameters statisticParameters;	
	private StatisticCalculator statisticCalculator;

	public MaxRangeOutlierFilter(final char c, final StatisticParameters statisticParameters) {
		super(c);
		this.threshold = 0.3;
		this.statisticParameters = statisticParameters;
		statisticCalculator = statisticParameters.getStatisticCalculator();
	}

	@Override
	public OutlierStorageFilter createInstance(final char c) {
		return new MaxRangeOutlierFilter(c, statisticParameters);
	}
	
	@Override
	public String getType() {
		return "maxRange";
	}
	
	@Override
	public void process(String line) {
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
			if (key.equals("threshold")) {
				threshold = Double.parseDouble(value);
			} else {
				throw new IllegalArgumentException("Invalid argument " + key + " IN: " + line);
			}
		}
	}
	
	@Override
	public boolean filter(final Result result, final Location location,	final AbstractWindowIterator windowIterator) {
		final ParallelPileup parallelPileup = result.getParellelPileup();
		
		if (parallelPileup.getN1() == 1 && parallelPileup.getN2() == 1) {
			return false;
		}
		
		if (parallelPileup.getN1() > 1 && parallelPileup.getN2() > 1) {
			Pileup[] pileups1 = getPileups(parallelPileup.getPooledPileup1().getAlleles(), parallelPileup.getPileups1());
			if (isOutlier(parallelPileup, pileups1)) {
				return true;
			}
			Pileup[] pileups2 = getPileups(parallelPileup.getPooledPileup2().getAlleles(), parallelPileup.getPileups2());
			if (isOutlier(parallelPileup, pileups2)) {
				return true;
			}
		}

		if (parallelPileup.getN1() > 1) {
			Pileup[] pileups1 = getPileups(parallelPileup.getPooledPileup1().getAlleles(), parallelPileup.getPileups1());
			if (isOutlier(parallelPileup, pileups1)) {
				return true;
			}
		}

		if (parallelPileup.getN2() > 1) {
			Pileup[] pileups2 = getPileups(parallelPileup.getPooledPileup2().getAlleles(), parallelPileup.getPileups2());
			if (isOutlier(parallelPileup, pileups2)) {
				return true;
			}
		}

		return false;
	}

	protected Pileup[] getPileups(final int[] baseIs, final Pileup[] allPileups) {
		int pileupI = -1;
		int pileupJ = -1;
		int maxRange = -1;

		for (int i = 0; i < allPileups.length; ++i) {
			for (int j = 0; j < allPileups.length; ++j) {
				for (int baseI : baseIs) {
					int range = Math.abs(allPileups[i].getCounts().getBaseCount(baseI) - allPileups[j].getCounts().getBaseCount(baseI));
					if (range > maxRange) {
						pileupI = i;
						pileupJ = j;
						maxRange = range;
					}
				}
			}
		}
		if (maxRange == -1) {
			return null;
		}
		final Pileup[] maxRangePileups = new Pileup[2];
		maxRangePileups[0] = allPileups[pileupI];
		maxRangePileups[1] = allPileups[pileupJ];

		return maxRangePileups;
	}

	private boolean isOutlier(final ParallelPileup parallelPileup, final Pileup[] pileups) {
		if (pileups == null) {
			return false;
		}

		Pileup[] pileups1 = new Pileup[]{pileups[0]};
		Pileup[] pileups2 = new Pileup[]{pileups[1]};
		ParallelPileup maxRangeParallelPileup = new DefaultParallelPileup(pileups1, pileups2);

		double maxRangeStat = statisticCalculator.getStatistic(maxRangeParallelPileup);
		
		if (threshold == -1.0) {
			double stat = statisticCalculator.getStatistic(parallelPileup);
			return maxRangeStat <= stat;
		} 

		return maxRangeStat <= threshold;
	}

	public StatisticParameters getStatisticParameters() {
		return statisticParameters;
	}

	public double getThreshold() {
		return threshold;
	}
	
}