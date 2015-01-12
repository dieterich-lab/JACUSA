package jacusa.filter;

import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.filter.storage.AbstractFilterStorage;
import jacusa.util.WindowCoordinates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterConfig implements Cloneable {

	private final Map<Character, AbstractFilterFactory<?>> c2Factory;
	private final List<AbstractFilterFactory<?>> i2Factory;
	private final Map<Character, Integer> c2i;
	
	public FilterConfig() {
		int initialCapacity = 6;

		c2Factory = new HashMap<Character, AbstractFilterFactory<?>>(initialCapacity);
		i2Factory = new ArrayList<AbstractFilterFactory<?>>(initialCapacity);
		c2i = new HashMap<Character, Integer>(initialCapacity);
	}

	/**
	 * 
	 * @param filterFactory
	 * @throws Exception
	 */
	public void addFactory(final AbstractFilterFactory<?> filterFactory) throws Exception {
		final char c = filterFactory.getC();

		if (c2Factory.containsKey(c)) {
			throw new Exception("Duplicate value: " + c);
		} else {
			c2i.put(c, i2Factory.size());
			i2Factory.add(filterFactory);
			c2Factory.put(c, filterFactory);	
		}
	}

	/**
	 * Create CountFilterCache for each available filter.
	 * Info: some filters might not need the cache
	 * 
	 * @return
	 */
	public FilterContainer createFilterContainer(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		AbstractFilterStorage<?>[] filters = new AbstractFilterStorage[i2Factory.size()];
		for (int filterI = 0; filterI < i2Factory.size(); ++filterI) {
			filters[filterI] = i2Factory.get(filterI).createFilterStorage(windowCoordinates, sampleParameters);
			
		}
		FilterContainer filterContainer = new FilterContainer(this, filters, windowCoordinates);

		return filterContainer;
	}

	public List<AbstractFilterFactory<?>> getFactories() {
		return i2Factory;
	}

	public boolean hasFiters() {
		return c2Factory.size() > 0;
	}

	public int c2i(char c) {
		return c2i.get(c);
	}
	
}