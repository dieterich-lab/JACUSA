package jacusa.pileup;


import java.util.HashMap;
import java.util.Map;

public class Result {

	private ParallelPileup parallelPileup;
	private double statistic;
	private Map<String, Object> data;
	private StringBuilder filterInfo;
	private StringBuilder info;
	
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
		if (filterInfo == null) {
			filterInfo = new StringBuilder();
		} else {
			filterInfo.append(":");
		}

		filterInfo.append(info);
	}

	public void addInfo(String s) {
		if (info == null) {
			info = new StringBuilder();
		} else {
			info.append(":");
		}

		info.append(s);
	}

	public String getInfo() {
		return info == null ? "*" : info.toString();
	}
	
	public String getFilterInfo() {
		return filterInfo == null ? "*" : filterInfo.toString();
	}

	public boolean hasFilterInfo() {
		return filterInfo != null;
	}

}