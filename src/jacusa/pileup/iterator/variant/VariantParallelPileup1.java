package jacusa.pileup.iterator.variant;

import jacusa.pileup.DefaultPileup.STRAND;
import jacusa.pileup.BaseConfig;
import jacusa.pileup.ParallelPileup;
import jacusa.pileup.Pileup;

public class VariantParallelPileup1 implements Variant {
	
	private final BaseConfig baseConfig; 
	
	public VariantParallelPileup1(final BaseConfig baseConfig) {
		this.baseConfig = baseConfig;
	}
	
	@Override
	public boolean isValid(ParallelPileup parallelPileup) {
		// more than one non-reference allele
		final Pileup pileup = parallelPileup.getPooledPileup1(); 
		
		if (pileup.getAlleles().length > 1) {
			return true;
		}

		// pick reference base by MD or by majority.
		// all other bases will be converted in pileup2 to refBaseI
		final char refBase = pileup.getRefBase();

		if (refBase != 'N') {
			int refBaseI = baseConfig.getBaseI((byte)refBase);
			if (parallelPileup.getStrand() == STRAND.REVERSE) {
				refBaseI = baseConfig.getComplementBaseI((byte)refBase);
			}

			// there has to be at least one non-reference base call in the data
			return pileup.getCoverage() - 
					pileup.getCounts().getBaseCount(refBaseI) > 0;
		}

		return false;
	}

}