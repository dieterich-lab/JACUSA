package jacusa.result;


import java.util.HashMap;
import java.util.Map;

import jacusa.pileup.ParallelPileup;

public class Result {

	private ParallelPileup parallelPileup;
	private double statistic;
	private Map<String, Object> data;
	private StringBuilder sb;
	
	public Result() {
		parallelPileup = null;
		statistic = Double.NaN;
		data = new HashMap<String, Object>(10);
	}

	
	public void setParellelPileup(ParallelPileup parallelPileup) {
		this.parallelPileup = parallelPileup;
	}

	
	public ParallelPileup getParellelPileup() {
		return parallelPileup;
	}

	
	public void setStatistic(double statistic) {
		this.statistic = statistic;
	}

	
	public double getStatistic() {
		return statistic;
	}

	
	public void setObject(String name, Object object) {
		data.put(name, object);
	}

	
	public Object getObject(String name) {
		if (! data.containsKey(name)) {
			return null;
		}

		return data.get(name);
	}

	
	public void addFilterInfo(String info) {
		if (sb == null) {
			sb = new StringBuilder();
		} else {
			sb.append(":");
		}

		sb.append(info);
	}

	
	public String getFilterInfo() {
		return sb.toString();
	}
	
}
