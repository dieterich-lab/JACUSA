package jacusa.filter.storage.outlier;

import jacusa.filter.AbstractStorageFilter;

public abstract class OutlierStorageFilter extends AbstractStorageFilter<Void>{

	protected OutlierStorageFilter(final char c) {
		super(c);
	}

	public abstract void process(String line);
	public abstract String getType();
	public abstract OutlierStorageFilter createInstance(final char c);

}
