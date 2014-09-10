package accusa2.cli.parameters;

import java.util.ArrayList;
import java.util.List;

import accusa2.filter.samtag.SamTagFilter;
import accusa2.pileup.BaseConfig;
import accusa2.pileup.builder.PileupBuilderFactory;
import accusa2.pileup.builder.UndirectedPileupBuilderFactory;

public class SampleParameters {

	// cache related
	private int maxDepth;

	// filter: read, base specific
	private byte minBASQ;
	private int minMAPQ;
	private int minCoverage;

	// filter: flags
	private int filterFlags;
	private int retainFlags;

	// filter based on SAM tags
	private List<SamTagFilter> samTagFilters;

	// dataA
	// path to BAM files
	private String[] pathnames;
	// properties for BAM files
	private BaseConfig baseConfig;
	private PileupBuilderFactory pileupBuilderFactory;
	
	public SampleParameters() {
		maxDepth 		= -1;
		minBASQ			= Byte.parseByte("20");
		minMAPQ 		= 20;
		minCoverage 	= 5;

		filterFlags 	= 0;
		retainFlags	 	= 0;

		samTagFilters 	= new ArrayList<SamTagFilter>();
		pathnames 		= new String[0];
		pileupBuilderFactory = new UndirectedPileupBuilderFactory();
	}

	/**
	 * @return the maxDepth
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * @param maxDepth the maxDepth to set
	 */
	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	/**
	 * @return the minBASQ
	 */
	public byte getMinBASQ() {
		return minBASQ;
	}

	/**
	 * @param minBASQ the minBASQ to set
	 */
	public void setMinBASQ(byte minBASQ) {
		this.minBASQ = minBASQ;
	}

	/**
	 * @return the minMAPQ
	 */
	public int getMinMAPQ() {
		return minMAPQ;
	}

	/**
	 * @param minMAPQ the minMAPQ to set
	 */
	public void setMinMAPQ(int minMAPQ) {
		this.minMAPQ = minMAPQ;
	}

	/**
	 * @return the minCoverage
	 */
	public int getMinCoverage() {
		return minCoverage;
	}

	/**
	 * @param minCoverage the minCoverage to set
	 */
	public void setMinCoverage(int minCoverage) {
		this.minCoverage = minCoverage;
	}

	/**
	 * @return the filterFlags
	 */
	public int getFilterFlags() {
		return filterFlags;
	}

	/**
	 * @param filterFlags the filterFlags to set
	 */
	public void setFilterFlags(int filterFlags) {
		this.filterFlags = filterFlags;
	}

	/**
	 * @return the retainFlags
	 */
	public int getRetainFlags() {
		return retainFlags;
	}

	/**
	 * @param retainFlags the retainFlags to set
	 */
	public void setRetainFlags(int retainFlags) {
		this.retainFlags = retainFlags;
	}

	/**
	 * @return the samTagFilters
	 */
	public List<SamTagFilter> getSamTagFilters() {
		return samTagFilters;
	}

	/**
	 * @param samTagFilters the samTagFilters to set
	 */
	public void setSamTagFilters(List<SamTagFilter> samTagFilters) {
		this.samTagFilters = samTagFilters;
	}

	/**
	 * @return the pathnames
	 */
	public String[] getPathnames() {
		return pathnames;
	}

	/**
	 * @param pathnames the pathnames to set
	 */
	public void setPathnames(String[] pathnames) {
		this.pathnames = pathnames;
	}

	/**
	 * @return the pileupBuilderFactory
	 */
	public PileupBuilderFactory getPileupBuilderFactory() {
		return pileupBuilderFactory;
	}

	/**
	 * @param pileupBuilderFactory the pileupBuilderFactory to set
	 */
	public void setPileupBuilderFactory(PileupBuilderFactory pileupBuilderFactory) {
		this.pileupBuilderFactory = pileupBuilderFactory;
	}

	public BaseConfig getBaseConfig() {
		return baseConfig;
	}

	public void setBaseConfig(BaseConfig baseConfig) {
		this.baseConfig = baseConfig;
	}

}