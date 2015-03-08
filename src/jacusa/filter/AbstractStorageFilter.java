package jacusa.filter;

import jacusa.io.format.result.BED6ResultFormat;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public abstract class AbstractStorageFilter<T> {

	private final char c;
	private String filterInfo;

	public AbstractStorageFilter(final char c) {
		this.c = c;
		filterInfo = new String();
	}
	
	public abstract boolean filter(final ParallelPileup parallelPileup, final Location location, AbstractWindowIterator windowIterator);
	
	public final char getC() {
		return c;
	}
	
	protected T getData(FilterContainer filterContainer) {
		int filterI = filterContainer.getFilterConfig().c2i(c);

		@SuppressWarnings("unchecked")
		T data = (T)filterContainer.get(filterI).getContainer();

		return data;
	}
	
	public final String getFilterInfo() {
		return filterInfo;
	}
	
	public final void setFilterInfo(String filterInfo) {
		this.filterInfo = filterInfo;
	}

	public final void resetFilterInfo() {
		setFilterInfo(Character.toString(BED6ResultFormat.EMPTY));
	}

}