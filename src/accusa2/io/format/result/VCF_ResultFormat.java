package accusa2.io.format.result;

import accusa2.pileup.ParallelPileup;

// TODO
public class VCF_ResultFormat extends AbstractResultFormat {

	public VCF_ResultFormat() {
		super('V', "VCF output");
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup, double value) {
		return null;
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		return null;
	}

	@Override
	public double extractValue(String line) {
		return 0;
	}

	@Override
	public char getCOMMENT() {
		return 0;
	}

	@Override
	public char getSEP() {
		return 0;
	}

	@Override
	public char getSEP2() {
		return 0;
	}

	@Override
	public char getEMPTY() {
		return 0;
	}

}