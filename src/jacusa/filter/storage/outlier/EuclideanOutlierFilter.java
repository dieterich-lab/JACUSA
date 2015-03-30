package jacusa.filter.storage.outlier;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.pileup.Pileup;

class EuclideanOutlierFilter extends MaxRangeOutlierFilter {

	public EuclideanOutlierFilter(final char c, final StatisticParameters statisticParameters) {
		super(c, statisticParameters);
	}

	@Override
	public OutlierStorageFilter createInstance(final char c) {
		return new EuclideanOutlierFilter(c, getStatisticParameters());
	}

	@Override
	public String getType() {
		return "euclidean";
	}
	
	@Override
	public void process(String line) {
		super.process(line);
	}

	@Override
	protected Pileup[] getPileups(final int[] baseIs, final Pileup[] allPileups) {
		int pileupI = -1;
		int pileupJ = -1;
		double dist = Double.MAX_VALUE;

		for (int i = 0; i < allPileups.length; ++i) {
			for (int j = 0; j < allPileups.length; ++j) {
				double tmpDist = 0.0;
				
				for (int baseI : baseIs) {
					tmpDist += Math.pow((double)allPileups[i].getCounts().getBaseCount(baseI) - (double)allPileups[j].getCounts().getBaseCount(baseI), 2d);
				}
				tmpDist = Math.sqrt(tmpDist);
				if (tmpDist < dist) {
					pileupI = i;
					pileupJ = j;
					dist = tmpDist;
				}
			}
		}
		if (dist == Double.MAX_VALUE) {
			return null;
		}
		final Pileup[] maxRangePileups = new Pileup[2];
		maxRangePileups[0] = allPileups[pileupI];
		maxRangePileups[1] = allPileups[pileupJ];

		return maxRangePileups;
	}

}