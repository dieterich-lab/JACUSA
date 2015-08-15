package jacusa.pileup;


import jacusa.util.Info;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Michael Piechotta
 */
public class Result {

	private ParallelPileup parallelPileup;
	private double statistic;
	private Map<String, Object> data;
	private Info filterInfo;
	private Info resultInfo;
	
	public Result() {
		parallelPileup = null;
		statistic = Double.NaN;
		data = new HashMap<String, Object>(10);
		filterInfo = new Info();
		resultInfo = new Info();
	}
	
	/**
	 * 
	 * @param parallelPileup
	 */
	public void setParellelPileup(ParallelPileup parallelPileup) {
		this.parallelPileup = parallelPileup;
	}
	
	/**
	 * 
	 * @return
	 */
	public ParallelPileup getParellelPileup() {
		return parallelPileup;
	}

	/**
	 * 
	 * @param statistic
	 */
	public void setStatistic(double statistic) {
		this.statistic = statistic;
	}
	
	/**
	 * 
	 * @return
	 */
	public double getStatistic() {
		return statistic;
	}

	/**
	 * 
	 * @param name
	 * @param object
	 */
	public void setObject(String name, Object object) {
		data.put(name, object);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public Object getObject(String name) {
		if (! data.containsKey(name)) {
			return null;
		}

		return data.get(name);
	}

	/**
	 * 
	 * @return
	 */
	public Info getResultInfo() {
		return resultInfo;
	}
	
	/**
	 * 
	 * @return
	 */
	public Info getFilterInfo() {
		return filterInfo;
	}

}