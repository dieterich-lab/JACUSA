package accusa2.filter.process;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import accusa2.cli.Parameters;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.pileup.Pileup;

public class PileupBuilderFilter implements Cloneable {

	private final Parameters parameters;

	private final Map<Character, AbstractFilterFactory> c2f;
	private final Map<Character, Integer> c2i;
	private final List<AbstractFilterFactory> i2f;

	public PileupBuilderFilter(Parameters paramters) {
		this.parameters = paramters;

		int n = 6;

		c2f = new HashMap<Character, AbstractFilterFactory>(n);
		c2i = new HashMap<Character, Integer>(n);
		i2f	= new ArrayList<AbstractFilterFactory>(n);
	}

	public void addFilterFactory(final AbstractFilterFactory filterFactory) throws Exception {
		filterFactory.setParameters(parameters);
		final char c = filterFactory.getC();
		if(c2f.containsKey(c)) {
			throw new Exception("Duplicate value: " + c);
		} else {
			c2i.put(c, i2f.size());
			i2f.add(filterFactory);
			c2f.put(c, filterFactory);
			
		}
	}

	public List<AbstractPileupBuilderFilter> getPileupBuilderFilters() {
		List<AbstractPileupBuilderFilter> pileupBuilderFilters = new ArrayList<AbstractPileupBuilderFilter>(c2f.size());
		for(int i = 0; i < i2f.size(); ++i) {
			pileupBuilderFilters.add(i2f.get(i).getPileupBuilderFilterInstance());
		}
		return pileupBuilderFilters;
	}
	
	public Pileup getFilteredPileup(char c, Pileup pileup) {
		if(pileup.getFilteredPileups().length == 0) {
			return null;
		}
		return pileup.getFilteredPileups()[c2i.get(c)];
	}

	public int getI(char c) {
		return c2i.get(c);
	}
	
	// FIXME pre-compute
	public List<AbstractParallelPileupFilter> getParallelPileupFilters() {
		List<AbstractParallelPileupFilter> parallelPileupFilters = new ArrayList<AbstractParallelPileupFilter>(c2f.size());
		for(int i = 0; i < i2f.size(); ++i) {
			parallelPileupFilters.add(i2f.get(i).getParallelPileupFilterInstance());
		}
		return parallelPileupFilters;
	}

	public List<AbstractFilterFactory> getFilterFactories() {
		return i2f;
	}

	public boolean hasFiters() {
		return c2f.size() > 0;
	}

}
