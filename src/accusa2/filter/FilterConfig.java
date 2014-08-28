package accusa2.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import accusa2.cli.Parameters;
import accusa2.filter.cache.AbstractPileupBuilderFilterCount;
import accusa2.filter.factory.AbstractFilterFactory;

public class FilterConfig implements Cloneable {

	private final Parameters parameters;
	private final Map<Character, AbstractFilterFactory> c2Factory;
	private final List<AbstractFilterFactory> i2Factory;
	private final Map<Character, Integer> c2i;

	public FilterConfig(Parameters paramters) {
		this.parameters = paramters;
		int n = 6;

		c2Factory = new HashMap<Character, AbstractFilterFactory>(n);
		i2Factory = new ArrayList<AbstractFilterFactory>(n);
		c2i = new HashMap<Character, Integer>(n);
	}

	/**
	 * 
	 * @param filterFactory
	 * @throws Exception
	 */
	public void addFactory(final AbstractFilterFactory filterFactory) throws Exception {
		filterFactory.setParameters(parameters);
		final char c = filterFactory.getC();

		if(c2Factory.containsKey(c)) {
			throw new Exception("Duplicate value: " + c);
		} else {
			c2i.put(c, i2Factory.size());
			i2Factory.add(filterFactory);
			c2Factory.put(c, filterFactory);	
		}
	}

	public AbstractPileupBuilderFilterCount[] createCache() {
		AbstractPileupBuilderFilterCount[] filterCache = new AbstractPileupBuilderFilterCount[c2Factory.size()];

		for(int i = 0; i < i2Factory.size(); ++i) {
			filterCache[i] = i2Factory.get(i).getFilterCountInstance();
		}

		return filterCache;
	}

	public List<AbstractFilterFactory> getFactories() {
		return i2Factory;
	}

	public boolean hasFiters() {
		return c2Factory.size() > 0;
	}

	public int c2i(char c) {
		return c2i.get(c);
	}

	
	
}