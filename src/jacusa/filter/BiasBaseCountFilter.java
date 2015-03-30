package jacusa.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import jacusa.filter.storage.bias.BaseCount;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.result.Result;
import jacusa.util.Location;
import jacusa.util.MathUtil;

public class BiasBaseCountFilter extends AbstractStorageFilter<BaseCount> {

	private final double probT = 0.1; // H-Test related
	private int dataSize;

	public BiasBaseCountFilter(final char c, final int dataSize) {
		super(c);
		this.dataSize = dataSize;
	}

	@Override
	public boolean filter(final Result result, Location location, AbstractWindowIterator windowIterator) {
		final ParallelPileup parallelPileup = result.getParellelPileup();
		
		FilterContainer[] replicateFilterContainer1 = windowIterator.getFilterContainers4Replicates1(location);
		FilterContainer[] replicateFilterContainer2 = windowIterator.getFilterContainers4Replicates2(location);

		// genomic position need to extract counts from filterContainer
 		int genomicPosition = parallelPileup.getStart();
		// [replicateI][baseI][dataI]
		int[][][] counts1 = getCounts(genomicPosition, replicateFilterContainer1);
		int[][][] counts2 = getCounts(genomicPosition, replicateFilterContainer2);

		// merged and sort data
		SampleBaseComparator rc = new SampleBaseComparator(counts1, counts2);
		// ordered index of data
		Integer[] index = rc.createUnorderedIndex();
		Arrays.sort(index, rc);

		if (! rc.check()) {
			return false;
		}

		/* debug info
		int[] values = rc.getValues(indexes);
		int[] base = rc.getBase(indexes);
		int[] group = rc.getGroup(indexes);
		int[] readPositionCount = rc.getReadPositionCount();
		*/

		double H = rc.getHStat(index);
		ChiSquareDist chi = new ChiSquareDist(rc.getGroupCount() - 1);
		double prob = chi.cdf(H);
		// convert to phred-score
		double phred = MathUtil.Prob2Phred(1- prob); 

		// filter positions with divergent distributions of data
		if (1 - prob <= probT) {
			// provide meta information
			result.addFilterInfo(getC() + "=" + Double.toString(phred));
			return true;
		}

		return false;
	}

	/**
	 * Extract data at genomicPosition from replicateFilterContainer and return
	 * array [replicateI][baseI][dataI]
	 * @param genomicPosition
	 * @param replicateFilterContainer
	 * @return array [replicateI][baseI][dataI]
	 */
	private int[][][] getCounts(final int genomicPosition, FilterContainer[] replicateFilterContainer) {
		int n = replicateFilterContainer.length;
		int baseLength = getData(replicateFilterContainer[0]).getBaseLength();
		int[][][] counts = new int[n][baseLength][dataSize];

		for (int replicateI = 0; replicateI < n; ++replicateI) {
			FilterContainer filterContainer = replicateFilterContainer[replicateI];
			
			BaseCount biasContainer = getData(filterContainer);
			int windowPosition = genomicPosition - filterContainer.getWindowCoordinates().getGenomicWindowStart();

			for (int baseI = 0; baseI < baseLength; baseI++) {
				int[] tmpBases = biasContainer.getData()[windowPosition][baseI];
				System.arraycopy(tmpBases, 0, counts[replicateI][baseI], 0, dataSize);
			}
		}

		return counts;
	}

	/**
	 * 
	 * @author mpiechotta
	 *
	 */
	private class SampleBaseComparator implements Comparator<Integer> {

		// histogram
		private int[] histogram;
		// counter for number of groups:
		// 
		private int groupI;

		// container(s) for
		private List<Integer> values;
		private List<Integer> base;
		private List<Integer> group;
		private List<Integer> groupCount;

		/**
		 * 
		 * @param counts1
		 * @param counts2
		 */
		public SampleBaseComparator(final int[][][] counts1, final int[][][] counts2) {
			histogram = new int[dataSize];
			groupI = -1;
			
			values = new ArrayList<Integer>(dataSize);
			base = new ArrayList<Integer>(dataSize);
			group = new ArrayList<Integer>(dataSize);
			groupCount = new ArrayList<Integer>(8);

			// fill containers
			process(counts1, 1);
			process(counts2, 2);
		}

		public int getGroupCount() {
			return groupI + 1;
		}

		/**
		 * Check if pre-conditions of H-Test are met (c_i >= 5 : V i in 1:k)
		 * @return
		 */
		public boolean check() {
			// check if sufficient data is available to perform H-Test
			return true;
			/*
			for (int count : groupCount) {
				if (count <= 5) {
					return false;
				}
			}

			return true;
			*/
		}

		/**
		 * 
		 * @param counts
		 * @param sampleI
		 */
		private void process(final int[][][] counts, int sampleI) {
			for (int replicateI = 0; replicateI < counts.length; ++replicateI) {

				int baseLength = counts[replicateI].length;
				for (int baseI = 0; baseI < baseLength; ++baseI) {

					// group counter indicator variable
					boolean counted = false;

					for (int dataI = 0; dataI < dataSize; ++dataI) {
						int count = counts[replicateI][baseI][dataI];
						if (count > 0) {
							if (! counted) {
								++groupI;
								counted = true;
								groupCount.add(0);
							}

							// consider count > 1
							for (int i = 0; i < count; ++i) {
								values.add(dataI);
								this.base.add(baseI);
								this.group.add(groupI);
							}
							
							// update groupCount and 
							int groupC = groupCount.get(groupI);
							groupCount.set(groupI, groupC + count);
							// read position histogram
							histogram[dataI] += count;
						}
					}
				}
			}
		}
		
		/**
		 * 
		 * @return
		 */
		public Integer[] createUnorderedIndex() {
			Integer[] indexes = new Integer[values.size()];
		    for (int i = 0; i < indexes.length; i++) {
	            indexes[i] = i;
	        }
		    return indexes;
		}

		/**
		 * 
		 * @param orderedIndex
		 * @return
		 */
		public int[] getValues(Integer[] orderedIndex) {
			int[] indexedValues = new int[orderedIndex.length];
			for (int i = 0; i < orderedIndex.length; ++i) {
				indexedValues[i] = values.get(orderedIndex[i]);
			}

			return indexedValues;
		}

		/**
		 * 
		 * @param orderedIndex
		 * @return
		 */
		public int[] getGroup(Integer[] orderedIndex) {
			int[] indexedGroup = new int[orderedIndex.length];
			for (int i = 0; i < orderedIndex.length; ++i) {
				indexedGroup[i] = group.get(orderedIndex[i]);
			}

			return indexedGroup;
		}

		/**
		 * 
		 * @param orderedIndex
		 * @return
		 */
		public double getHStat(Integer[] orderedIndex) {
			// number of observations
			double n = (double)orderedIndex.length;

			// first factor
			double H1 = 12d / (n * (n + 1d));

			// summed rank per group
			double[] Sh = new double[getGroupCount()];
			// number of observations per group
			int[] nh = new int[getGroupCount()];

			int[] orderedReadPosition = getValues(orderedIndex);
			int[] orderedGroup = getGroup(orderedIndex);

			int rank = 1;
			for (int i = 0; i < orderedIndex.length;) {
				int readPosition = orderedReadPosition[i];
				int count = histogram[readPosition];

				double tmpRank = (double)(rank);
				nh[orderedGroup[i]] += 1;
				// ranks with multiple readPositions are split
				// 2x readPosition at rank 3 and 4 => 3.5 average rank for both observations
				if (count > 1) {
					for (int j = 1; j < count; ++j) {
						tmpRank += (double)(rank + j);
						nh[orderedGroup[i + j]] += 1;	
					}
					tmpRank /= (double)count;
					for (int j = 0; j < count; ++j) {
						Sh[orderedGroup[i + j]] += tmpRank;
					}
				} else {
					Sh[orderedGroup[i]] += tmpRank;
				}
				
				rank += count;
				i += count;
			}

			// second factor
			double H2 = 0.0;
			for (int i = 0; i < getGroupCount(); ++i) {
				H2 += (double)Sh[i] * (double)Sh[i] / (double)nh[i];
			}

			// combing factors
			return H1 * H2 - 3d * (n + 1d);
		}

		@Override
		public int compare(Integer o1, Integer o2) {
			return values.get(o1).compareTo(values.get(o2));
		}

	}

}