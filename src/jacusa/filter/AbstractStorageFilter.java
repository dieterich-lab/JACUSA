package jacusa.filter;

import jacusa.pileup.Result;
import jacusa.pileup.iterator.AbstractWindowIterator;
import jacusa.util.Location;

public abstract class AbstractStorageFilter<T> {

	private final char c;

	public AbstractStorageFilter(final char c) {
		this.c = c;
	}
	
	public final char getC() {
		return c;
	}
	
	protected T getData(FilterContainer filterContainer) {
		int filterI = filterContainer.getFilterConfig().c2i(c);

		@SuppressWarnings("unchecked")
		T data = (T)filterContainer.get(filterI).getContainer();

		return data;
	}

	protected abstract boolean filter(
			final Result result, 
			final Location location, 
			final AbstractWindowIterator windowIterator);
	
	public boolean applyFilter(
			final Result result, 
			final Location location, 
			final AbstractWindowIterator windowIterator) {
		if (filter(result, location, windowIterator)) {
			addFilterInfo(result);
			return true;
		}
		
		return false;
	}

	public void addFilterInfo(Result result) {
		result.addFilterInfo(Character.toString(getC()));
	}

}