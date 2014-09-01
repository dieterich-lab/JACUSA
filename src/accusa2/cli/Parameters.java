package accusa2.cli;

import java.util.ArrayList;
import java.util.List;

import accusa2.filter.FilterConfig;
import accusa2.filter.samtag.SamTagFilter;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.format.DefaultResultFormat;
import accusa2.io.output.Output;
import accusa2.io.output.OutputPrinter;
import accusa2.method.AbstractMethodFactory;
import accusa2.method.statistic.LRStatistic;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.pileup.builder.UndirectedPileupBuilderFactory;

public class Parameters {

	// cache related
	private int windowSize;
	private int maxDepth;

	// filter: read, base specific
	private byte minBASQ;
	private int minMAPQ;
	private int minCoverage;

	private BaseConfig baseConfig;

	// filter: flags
	private int filterFlags;
	private int retainFlags;

	// filter: statistic
	private double stat;

	private int permutations;

	private int maxThreads;

	// output format related
	private Output output;
	private AbstractResultFormat resultFormat;

	// version
	public final String VERSION = "2.7 BETA";

	// bed file to scan for variants
	private String bedPathname;

	// chosen instances
	private AbstractMethodFactory methodFactory;
	private StatisticCalculator statisticCalculator;
	private FilterConfig filterConfig;

	// path to BAM files
	private String[] pathnamesA;
	private String[] pathnamesB;

	// properties for BAM files
	private PileupBuilderFactory pileupBuilderFactoryA;
	private PileupBuilderFactory pileupBuilderFactoryB;

	private List<SamTagFilter> samTagFilters;

	// debug flag
	private boolean debug; 

	private static Parameters singleton;
	
	public static Parameters getInstance() {
		if (singleton == null) {
			singleton = new Parameters();
		}
		
		return singleton;
	}
	
	/**
	 * 
	 */
	public Parameters() {
		windowSize 		= 10000;

		minBASQ 		= Byte.parseByte(new String("20"));
		minMAPQ 		= 20;
		minCoverage 	= 3;
		maxDepth 		= -1;
		maxThreads 		= 1;

		baseConfig			= new BaseConfig(BaseConfig.VALID);

		stat 			= 0.3;

		permutations	= 10;
		filterFlags		= 0;
		retainFlags		= 0;

		output 			= new OutputPrinter();
		resultFormat	= new DefaultResultFormat(this);

		bedPathname 	= new String();

		statisticCalculator	= new LRStatistic(this);

		filterConfig = new FilterConfig(this);

		pileupBuilderFactoryA	= new UndirectedPileupBuilderFactory();
		pileupBuilderFactoryB	= new UndirectedPileupBuilderFactory();

		samTagFilters 	= new ArrayList<SamTagFilter>(3);
		debug			= false;
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * 
	 * @param maxDepth
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	/**
	 * 
	 * @return
	 */
	public String[] getPathnamesA() {
		return pathnamesA;
	}

	/**
	 * 
	 * @param pathname1
	 */
	public void setPathnamesA(String[] pathnames) {
		this.pathnamesA = pathnames; 
	}

	/**
	 * 
	 * @return
	 */
	public String[] getPathnamesB() {
		return pathnamesB;
	}

	/**
	 * 
	 * @param pathname2
	 */
	public void setPathnamesB(String[] pathnames) {
		this.pathnamesB = pathnames; 
	}

	/**
	 * 
	 * @return
	 */
	public int getWindowSize() {
		return windowSize;
	}

	/**
	 * 
	 * @param windowSize
	 */
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}

	/**
	 * 
	 * @return
	 */
	public int getMinMAPQ() {
		return minMAPQ;
	}

	/**
	 * 
	 * @param minMAPQ
	 */
	public void setMinMAPQ(int minMAPQ) {
		this.minMAPQ = minMAPQ;
	}

	/**
	 * 
	 * @return
	 */
	public byte getMinBASQ() {
		return minBASQ;
	}

	/**
	 * 
	 * @param minBASQ
	 */
	public void setMinBASQ(byte minBASQ) {
		this.minBASQ = minBASQ;
	}

	/**
	 * 
	 * @return
	 */
	public int getMinCoverage() {
		return minCoverage;
	}

	/**
	 * 
	 * @param minCoverage
	 */
	public void setMinCoverage(int minCoverage) {
		this.minCoverage = minCoverage;
	}

	/**
	 * 
	 * @return
	 */
	public int getMaxThreads() {
		return maxThreads;
	}

	public int getReplicatesA() {
		return pathnamesA.length;
	}
	
	public int getReplicatesB() {
		return pathnamesB.length;
	}
	
	/**
	 * 
	 * @param maxThreads
	 */
	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	/**
	 * 
	 * @param output
	 */
	public void setOutput(Output output) {
		this.output = output;
	}

	/**
	 * 
	 * @return
	 */
	public Output getOutput() {
		return output;
	}

	/**
	 * 
	 * @param stat
	 */
	public void setStat(double stat) {
		this.stat = stat;
	}

	/**
	 * 
	 * @return
	 */
	public double getStat() {
		return stat;
	}

	/**
	 * 
	 * @param flags
	 */
	public void setFilterFlags(int flags) {
		this.filterFlags = flags;
	}

	/**
	 * 
	 * @return
	 */
	public int getFilterFlags() {
		return filterFlags;
	}

	/**
	 * 
	 * @param retainFlags
	 */
	public void setRetainFlags(int retainFlags) {
		this.retainFlags = retainFlags;
	}

	/**
	 * 
	 * @return
	 */
	public int getRetainFlags() {
		return retainFlags;
	}

	/**
	 * 
	 * @return
	 */
	public int getPermutations() {
		return permutations;
	}

	/**
	 * 
	 * @param permutations
	 */
	public void setPermutations(int permutations) {
		this.permutations = permutations;
	}

	/**
	 * 
	 * @return
	 */
	public AbstractResultFormat getResultFormat() {
		return resultFormat;
	}

	/**
	 * 
	 * @param resultFormat
	 */
	public void setResultFormat(AbstractResultFormat resultFormat) {
		this.resultFormat = resultFormat;
	}
	
	/**
	 * 
	 * @param workerFactory
	 */
	public void setWorkerFactory(AbstractMethodFactory workerFactory) {
		this.methodFactory = workerFactory;
	}

	/**
	 * 
	 * @return
	 */
	public AbstractMethodFactory getMethodFactory() {
		return methodFactory;
	}

	/**
	 * 
	 * @param statistic
	 */
	public void setStatistic(StatisticCalculator statistic) {
		this.statisticCalculator = statistic;
	}

	public StatisticCalculator getStatisticCalculator() {
		return statisticCalculator;
	}

	public void setMethodFactory(AbstractMethodFactory factory) {
		this.methodFactory = factory;
	}

	public void setBED_Pathname(String bedPathname) {
		this.bedPathname = bedPathname;
	}

	public String getBED_Pathname() {
		return bedPathname;
	}

	public BaseConfig getBaseConfig() {
		return baseConfig;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean getDebug() {
		return debug;
	}

	public PileupBuilderFactory getPileupBuilderFactoryA(){
		return pileupBuilderFactoryA;
	}

	public PileupBuilderFactory getPileupBuilderFactoryB(){
		return pileupBuilderFactoryB;
	}
	
	public List<SamTagFilter> getSamTagFilter() {
		return samTagFilters;
	}

	public void setPileupBuilderFactoryA(PileupBuilderFactory pileupBuilderFactory) {
		pileupBuilderFactoryA = pileupBuilderFactory;
	}

	public void setPileupBuilderFactoryB(PileupBuilderFactory pileupBuilderFactory) {
		pileupBuilderFactoryB = pileupBuilderFactory;
	}

	public FilterConfig getFilterConfig() {
		return filterConfig;
	}

}