package accusa2.io.format;

import accusa2.pileup.ParallelPileup;

public abstract class AbstractResultFormat {

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

	public abstract String convert2String(ParallelPileup parallelPileup, double value);
	public abstract String convert2String(ParallelPileup parallelPileup);

	public String getHeader(ParallelPileup parallelPileup) {
		return new String();
	}

	public abstract double extractValue(String line);

	public abstract char getCOMMENT();
	public abstract char getSEP();
	public abstract char getSEP2();
	public abstract char getEMPTY();
	
}
