package accusa2.io.format;

import accusa2.pileup.ParallelPileup;

/**
 * File format for tmp files. Wrapper class.
 * @author michael
 *
 */
public class TmpResultFormat extends AbstractResultFormat {

	private AbstractResultFormat resultFormat;
	
	/**
	 * 
	 * @param parameters
	 */
	public TmpResultFormat(AbstractResultFormat resultFormat) {
		super(resultFormat.getC(), resultFormat.getDesc());
		this.resultFormat = resultFormat;
	}

	@Override
	public String getHeader() {
		return resultFormat.getHeader();
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		return resultFormat.convert2String(parallelPileup);
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup, double value) {
		return resultFormat.convert2String(parallelPileup, value);
	}

	@Override
	public ParallelPileup extractParallelPileup(String line) {
		return resultFormat.extractParallelPileup(line);
	}

	@Override
	public double extractValue(String line) {
		return resultFormat.extractValue(line);
	}

	@Override
	public char getCOMMENT() {
		return resultFormat.getCOMMENT();
	}

	@Override
	public char getEMPTY() {
		return resultFormat.getEMPTY();
	}

	@Override
	public char getSEP() {
		return resultFormat.getSEP();
	}

	@Override
	public char getSEP2() {
		return resultFormat.getSEP2();
	}
	
}
