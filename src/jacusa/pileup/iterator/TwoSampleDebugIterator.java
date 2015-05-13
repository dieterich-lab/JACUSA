package jacusa.pileup.iterator;

import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.filter.FilterContainer;
import jacusa.pileup.ParallelPileup;
import jacusa.util.Coordinate;
import jacusa.util.Location;

public class TwoSampleDebugIterator extends AbstractWindowIterator {

	private boolean hasNext;
	
	public TwoSampleDebugIterator(ParallelPileup parallelPileup, TwoSampleCallParameters parameters) {
		super(new Coordinate("TODO", 1, 1), null, parameters);
		this.parallelPileup = parallelPileup;
		hasNext = true;
	}

	@Override
	public boolean hasNext() {
		return hasNext;
	}

	@Override
	public Location next() {
		hasNext = false;
		return new Location(parallelPileup.getContig(), parallelPileup.getEnd(), parallelPileup.getStrand());
	}
	
	@Override
	public FilterContainer[] getFilterContainers4Replicates1(Location location) {
		return null;
	}

	@Override
	public FilterContainer[] getFilterContainers4Replicates2(Location location) {
		return null;
	}

	@Override
	public int getAlleleCount(Location location) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getAlleleCount1(Location location) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public int getAlleleCount2(Location location) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}