package accusa2.io.format.result;

import accusa2.pileup.ParallelPileup;

// TODO
public class VCF_ResultFormat extends AbstractResultFormat {

	public VCF_ResultFormat() {
		super('V', "VCF output");
	}
	
	@Override
	public String convert2String(ParallelPileup parallelPileup, double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double extractValue(String line) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char getCOMMENT() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char getSEP() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char getSEP2() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public char getEMPTY() {
		// TODO Auto-generated method stub
		return 0;
	}

}