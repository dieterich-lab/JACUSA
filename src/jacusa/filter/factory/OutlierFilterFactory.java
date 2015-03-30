package jacusa.filter.factory;


import java.util.HashMap;
import java.util.Map;

import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.StatisticParameters;
import jacusa.filter.AbstractStorageFilter;
import jacusa.filter.storage.DummyFilterFillCache;
import jacusa.filter.storage.outlier.OutlierStorageFilter;
import jacusa.filter.storage.outlier.VarianceOutlierFilter;

import jacusa.util.WindowCoordinates;
// FINISIH
public class OutlierFilterFactory extends AbstractFilterFactory<Void> {

	private OutlierStorageFilter filter;
	private Map<String, OutlierStorageFilter> type2filter;
	
	public OutlierFilterFactory(StatisticParameters statisticParameters) {
		super('O', "Outlier filter");

		type2filter = new HashMap<String, OutlierStorageFilter>();
		OutlierStorageFilter[] filters = new OutlierStorageFilter[] {
				new VarianceOutlierFilter(getC()),
		};
		for (OutlierStorageFilter filter : filters) {
			type2filter.put(filter.getType(), filter);
		}
	}

	@Override
	public void processCLI(String line) {
		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));

		for (int i = 1; i < s.length; ++i) {
			// key=value
			String[] kv = s[i].split("=");
			String key = kv[0];
			String value = new String();
			if (kv.length == 2) {
				value = kv[1];
			}

			// set value
			if (key.equals("type") && type2filter.keySet().contains(value)) {
				filter = type2filter.get(value).createInstance(getC());
				filter.process(line);
			} else {
				throw new IllegalArgumentException("Invalid argument " + key + " IN: " + line);
			}
		}
	}

	public AbstractStorageFilter<Void> createStorageFilter() {
		return filter;
	}

	@Override
	public DummyFilterFillCache createFilterStorage(final WindowCoordinates windowCoordinates, final SampleParameters sampleParameters) {
		return new DummyFilterFillCache(getC());
	}

	
}
		
		
		