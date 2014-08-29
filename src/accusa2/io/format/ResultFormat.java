package accusa2.io.format;

import accusa2.pileup.ParallelPileup;

public interface ResultFormat extends OutputFormat {

	public abstract String convert2String(ParallelPileup parallelPileup, double value);
	public double extractValue(String line);

}