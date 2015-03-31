package jacusa.filter.factory;

import java.util.ArrayList;
import java.util.List;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.DefaultParallelPileup;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

public class ZeroCountFilterFactory extends AbstractFilterFactory<Void> {

	private StatisticParameters statisticParameters;	
	private StatisticCalculator statisticCalculator;
	
	public ZeroCountFilterFactory(StatisticParameters statisticParameters) {
		super('Z', "Robust zero count filter");
		this.statisticParameters = statisticParameters;

		statisticCalculator = statisticParameters.getStatisticCalculator();
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
	}

	public ZeroCountFilter createStorageFilter() {
		return new ZeroCountFilter(getC());
	}

	@Override
	public DummyFilterFillCache createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}
	
	private class ZeroCountFilter extends AbstractStorageFilter<Void>{
		
		public ZeroCountFilter(final char c) {
			super(c);
		}
		
		@Override
		public boolean filter(final Result result, final Location location,	final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();
			int a1 = parallelPileup.getPooledPileup1().getAlleles().length;
			int a2 = parallelPileup.getPooledPileup2().getAlleles().length;
			int[] alleles = parallelPileup.getPooledPileup().getAlleles();
			int aP = alleles.length;
			
			List<Integer> variantBaseIs = new ArrayList<Integer>(alleles.length);
			int targetBaseI = -1;
			for (int baseI : alleles) {
				int count1 = parallelPileup.getPooledPileup1().getCounts().getBaseCount(baseI);
				int count2 = parallelPileup.getPooledPileup2().getCounts().getBaseCount(baseI);

				if (count1 == 0 && count2 > 0 || count2 == 0 && count1 > 0) {
					variantBaseIs.add(baseI);
				} else if (count1 > 0 && count2  > 0) {
					targetBaseI = baseI;
				}
			}
			if (variantBaseIs.size() == 0) {
				return false;
			}

			ParallelPileup pp = null;
			if (a1 > 1 && a2 == 1 && aP == 2) {
				pp = new DefaultParallelPileup(parallelPileup.getPileups1(), parallelPileup.getPileups1());
				pp.setPileups1(flat(pp.getPileups1(), variantBaseIs, targetBaseI));
				
			} else if (a2 > 1 && a1 == 1 && aP == 2) {
				pp = new DefaultParallelPileup(parallelPileup.getPileups2(), parallelPileup.getPileups2());
				pp.setPileups2(flat(pp.getPileups2(), variantBaseIs, targetBaseI));
			}
			
			if (pp == null) {
				return false;
			}

			double statistic = statisticCalculator.getStatistic(pp);
			System.err.println(statistic);
			return statistic > statisticParameters.getThreshold();
		}

		private Pileup[] flat(Pileup[] pileups, List<Integer> variantBaseIs, int baseI) {
			Pileup[] ret = new Pileup[pileups.length];
			for (int i = 0; i < pileups.length; ++i) {
				ret[i] = new DefaultPileup(pileups[i]);
				
				for (int variantI : variantBaseIs) {
					// base
					ret[i].getCounts().getBaseCount()[baseI] += ret[i].getCounts().getBaseCount()[variantI];
					ret[i].getCounts().getBaseCount()[variantI] = 0;
					
					// qual
					for (int qualI = ret[i].getCounts().getMinQualI(); qualI < ret[i].getCounts().getQualCount()[variantI].length; ++qualI) {
						ret[i].getCounts().getQualCount()[baseI][qualI] += ret[i].getCounts().getQualCount()[variantI][qualI];
						ret[i].getCounts().getQualCount()[variantI][qualI] = 0;
					}
				}
			}
			return ret;
		}
	}
}
		
		
		