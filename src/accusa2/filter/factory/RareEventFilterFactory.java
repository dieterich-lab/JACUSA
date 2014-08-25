package accusa2.filter.factory;

import accusa2.filter.cache.AbstractPileupBuilderFilterCache;
import accusa2.filter.cache.RareEventParallelPileupFilter;

public class RareEventFilterFactory extends AbstractFilterFactory {

	private int reads = 2;
	private double level = 0.1;

	public RareEventFilterFactory() {
		super('R', "");
		desc = "Rare event filter. Default: reads:level " + Integer.toString(reads) + ":" + Double.toString(level);
	}

	@Override
	public RareEventParallelPileupFilter getFilterInstance() {
		return new RareEventParallelPileupFilter(getC(), getReads(), getLevel());
	}

	@Override
	public AbstractPileupBuilderFilterCache getCacheInstance() {
		return null;
	}

	@Override
	public void processCLI(String line) throws IllegalArgumentException {
		if(line.length() == 1) {
			throw new IllegalArgumentException("Invalid argument " + line);
		}

		String[] s = line.split(Character.toString(AbstractFilterFactory.SEP));
		// format R:reads:level 
		for(int i = 1; i < s.length; ++i) {

			switch(i) {
			case 1:
				setReads(Integer.valueOf(s[i]));
				break;

			case 2:
				setLevel(Double.valueOf(s[i]));
				break;

			default:
				throw new IllegalArgumentException("Invalid argument " + s[i]);
			}
		}
	}

	public final void setReads(int reads) {
		this.reads = reads;
	}

	public final int getReads() {
		return reads;
	}

	public final void setLevel(double level) {
		this.level = level;
	}

	public final double getLevel() {
		return level;
	}

}