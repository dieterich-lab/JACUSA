package jacusa.io.format.result;

import jacusa.io.format.output.AbstractOutputFormat;
import jacusa.pileup.ParallelPileup;

public abstract class AbstractResultFormat extends  AbstractOutputFormat {

	public AbstractResultFormat(char c, String desc) {
		super(c, desc);
	}

	public abstract double extractValue(String line);
	public abstract String convert2String(ParallelPileup parallelPileup, double value, String filterInfo);
	public abstract String getFilterInfo(String line);
	
}