package accusa2.io.format.result;

import accusa2.io.format.output.AbstractOutputFormat;
import accusa2.pileup.ParallelPileup;

public abstract class AbstractResultFormat extends  AbstractOutputFormat {

	public AbstractResultFormat(char c, String desc) {
		super(c, desc);
	}

	public abstract double extractValue(String line);
	public abstract String convert2String(ParallelPileup parallelPileup, double value);
	
}