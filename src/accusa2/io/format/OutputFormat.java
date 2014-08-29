package accusa2.io.format;

import accusa2.pileup.ParallelPileup;

public interface OutputFormat {

	char getC();
	String getDesc();

	String getHeader(ParallelPileup parallelPileup);
	public String convert2String(ParallelPileup parallelPileup);

	public char getCOMMENT();
	public char getSEP();
	public char getSEP2();
	public char getEMPTY();

}