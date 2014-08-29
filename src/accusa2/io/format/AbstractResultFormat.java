package accusa2.io.format;

import accusa2.pileup.ParallelPileup;

public abstract class AbstractResultFormat implements ResultFormat {

	private char c;
	private String desc;
	
	public AbstractResultFormat(char c, String desc) {
		this.c = c;
		this.desc = desc;
	}

	public final char getC() {
		return c;
	}

	public final String getDesc() {
		return desc;
	}

	public String getHeader(ParallelPileup parallelPileup) {
		return new String();
	}

	public abstract double extractValue(String line);

}