package jacusa.io.format.output;

import jacusa.pileup.ParallelPileup;

/**
 * File format for tmp files. Wrapper class.
 * @author michael
 *
 */
public class TmpOutputFormat extends AbstractOutputFormat {

	private AbstractOutputFormat resultFormat;
	
	/**
	 * 
	 * @param parameters
	 */
	public TmpOutputFormat(AbstractOutputFormat resultFormat) {
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