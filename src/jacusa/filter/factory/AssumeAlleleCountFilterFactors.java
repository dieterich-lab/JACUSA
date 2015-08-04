package jacusa.filter.factory;

//import java.util.Arrays;
//import java.util.Comparator;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.pileup.DefaultPileup;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;
import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;
import jacusa.util.WindowCoordinates;

@Deprecated
public class AssumeAlleleCountFilterFactors extends AbstractFilterFactory<Void> {

	private static int ALLELE_COUNT = 2;
	private int alleleCount;
	
	public AssumeAlleleCountFilterFactors() {
		super(
				'X', 
				"Max allowed alleles per parallel pileup. Default: "+ ALLELE_COUNT);
		alleleCount = ALLELE_COUNT;
	}
	
	@Override
	public DummyFilterFillCache createFilterStorage(
			WindowCoordinates windowCoordinates,
			SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}

	@Override
	public AbstractStorageFilter<Void> createStorageFilter() {
		return new AssumeAlleleCountFilter(getC());
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if (line.length() == 1) {
			return;
		}

		final String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		for (int i = 1; i < s.length; ++i) {
			switch(i) {
			case 1:
				final int alleleCount = Integer.valueOf(s[i]);
				if (alleleCount < 0) {
					throw new IllegalArgumentException("Invalid allele count " + line);
				}
				this.alleleCount = alleleCount;
				break;
				
			default:
				throw new IllegalArgumentException("Invalid argument: " + line);
			}
		}
	}
	
	private class AssumeAlleleCountFilter extends AbstractStorageFilter<Void> {
		
		public AssumeAlleleCountFilter(final char c) {
			super(c);
		}
		
		@Override
		public boolean filter(final Result result, final Location location, final AbstractWindowIterator windowIterator) {
			final ParallelPileup parallelPileup = result.getParellelPileup();
			if (parallelPileup.getPooledPileup().getAlleles().length <= alleleCount) {
				return false;
			}

			Pileup[] prunedPileup1 = prune(parallelPileup.getPileups1(), alleleCount);
			Pileup[] prunedPileup2 = prune(parallelPileup.getPileups2(), alleleCount);
			ParallelPileup prunedParallelPileup = parallelPileup.copy();
			prunedParallelPileup.setPileups1(prunedPileup1);
			prunedParallelPileup.setPileups2(prunedPileup2);

			if (prunedParallelPileup.getPooledPileup().getAlleles().length > alleleCount) {
				return true;
			}
			
			return false;
		}

		private Pileup[] prune(Pileup[] pileups, int targetAlleleCount) {
			Pileup[] copy = new Pileup[pileups.length];
			for (int i = 0; i < pileups.length; ++i) {
				// sort by base count
				// int[] bc = pileups[i].getCounts().getBaseCount();
				// BaseCountComparator bcc = new BaseCountComparator(bc);
				// 
				// Arrays.sort(index, bcc);

				// adjust base count and qual count 
				copy[i] = new DefaultPileup(pileups[i]);
				// copy[i].getCounts().reset();
				for (int j = 0; j < targetAlleleCount; ++j) {
					// int b = index[j];

					/* copy[i].getCounts().getBaseCount()[b] = pileups[i].getCounts().getBaseCount()[b];
					System.arraycopy(
							pileups[i].getCounts().getQualCount()[b], 
							0, 
							copy[i].getCounts().getQualCount()[b], 
							0, 
							pileups[i].getCounts().getQualCount()[b].length);
					*/
					
				}
			}
			return copy;
		}

		/*
		private class BaseCountComparator implements Comparator<Integer> {
		    private final int[] a;

		    public BaseCountComparator(final int[] a) {
		        this.a = a;
		    }

		    public Integer[] createIndexArray() {
		        Integer[] index = new Integer[a.length];
		        for (int i = 0; i < a.length; i++) {
		            index[i] = i;
		        }
		        return index;
		    }
		    
		    @Override
		    public int compare(Integer i1, Integer i2) {
		        return ((Integer)a[i1]).compareTo((Integer)a[i2]);
		    }
		}
		 */
	}
	
}
