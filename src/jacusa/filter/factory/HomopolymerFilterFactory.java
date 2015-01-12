package jacusa.filter.factory;

import jacusa.cli.parameters.AbstractParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.HomopolymerStorageFilter;
import jacusa.filter.storage.HomopolymerFilterStorage;
import jacusa.pileup.builder.WindowCache;

public class HomopolymerFilterFactory extends AbstractFilterFactory<WindowCache> {

	private int length = 5;
	private AbstractParameters parameters;
	
	public HomopolymerFilterFactory(AbstractParameters parameters) {
		super('Y', "Filter wrong variant calls in the vicinity of homopolymers. Format: length:distance");
		this.parameters = parameters;
	}
	
	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format Y:length:distance 
		for(int i = 1; i < s.length; ++i) {
			int value = Integer.valueOf(s[i]);

			switch(i) {
			case 1:
				setLength(value);
				break;

			default:
				throw new IllegalArgumentException("Invalid argument " + length);
			}
		}
	}

	@Override
	public HomopolymerFilterStorage createFilterStorage() {
		return new HomopolymerFilterStorage(getC(), length, parameters);
	}

	@Override
	public AbstractStorageFilter<WindowCache> createStorageFilter() {
		return new HomopolymerStorageFilter(c, parameters.getBaseConfig(), parameters.getFilterConfig());
	}

	public final void setLength(int length) {
		this.length = length;
	}

	public final int getLength() {
		return length;
	}

}
