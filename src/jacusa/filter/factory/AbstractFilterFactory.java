package jacusa.filter.factory;

import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.AbstractFilterStorage;

public abstract class AbstractFilterFactory<T> {

	public final static char SEP = ':';

	protected char c;
	protected String desc;

	public AbstractFilterFactory(char c, String desc) {
		this.c = c;
		this.desc = desc;
	}

	public abstract AbstractFilterStorage<T> createFilterStorage();
	public abstract AbstractStorageFilter<T> createStorageFilter();

	public char getC() {
		return c;
	}

	public String getDesc() {
		return desc;
	}

	public void processCLI(final String line) throws IllegalArgumentException {
		// implement to change behavior via CLI
	}

}