package jacusa.io.format.result;

import jacusa.pileup.ParallelPileup;

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
	public String getHeader(ParallelPileup parallelPileup) {
		return resultFormat.getHeader(parallelPileup);
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		return resultFormat.convert2String(parallelPileup);
	}

	@Override
	public String convert2String(final ParallelPileup parallelPileup, final double value, final String filterInfo) {
		return resultFormat.convert2String(parallelPileup, value, filterInfo);
	}

	@Override
	public double extractValue(String line) {
		return resultFormat.extractValue(line);
	}

	@Override
	public String getFilterInfo(String line) {
		return resultFormat.getFilterInfo(line);
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