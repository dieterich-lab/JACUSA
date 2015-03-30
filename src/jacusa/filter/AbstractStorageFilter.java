package jacusa.filter;

import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.result.Result;
import jacusa.util.Location;

public abstract class AbstractStorageFilter<T> {

	private final char c;

	public AbstractStorageFilter(final char c) {
		this.c = c;
	}
	
	public abstract boolean filter(
			final Result result, 
			final Location location, 
			final AbstractWindowIterator windowIterator);
	
	public final char getC() {
		return c;
	}
	
	protected T getData(FilterContainer filterContainer) {
		int filterI = filterContainer.getFilterConfig().c2i(c);

		@SuppressWarnings("unchecked")
		T data = (T)filterContainer.get(filterI).getContainer();

		return data;
	}

}