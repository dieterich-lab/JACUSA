package accusa2.io.format;

import net.sf.samtools.SAMUtils;
import accusa2.cli.Parameters;
import accusa2.pileup.DefaultPileup.STRAND;
import accusa2.pileup.ParallelPileup;
import accusa2.pileup.Pileup;
import accusa2.process.phred2prob.Phred2Prob;


// TODO
// add ref remove strand
public class PileupFormat extends AbstractResultFormat {

	public static char EMPTY 	= '*';
	public static char COMMENT = '#';
	public static char SEP 	= '\t';
	public static char SEP2 	= ',';

	public PileupFormat(char c, String desc) {
		super(c, desc);
	}

	public PileupFormat() {
		super('M', "samtools mpileup like format (base columns without: $ ^ < > *)");
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup, double value) {
		return convert2String(parallelPileup);
	}

	@Override
	public String convert2String(ParallelPileup parallelPileup) {
		StringBuilder sb = new StringBuilder();

		// coordinates
		sb.append(parallelPileup.getContig());
		sb.append(SEP);
		sb.append(parallelPileup.getPosition());

		addPileups(sb, parallelPileup.getStrandA(), parallelPileup.getPileupsA());
		addPileups(sb, parallelPileup.getStrandB(), parallelPileup.getPileupsB());

		return sb.toString();		
	}
	
	@Override
	public double extractValue(String line) {
		return -1.0;
	}
	
	protected void addPileups(StringBuilder sb, STRAND strand, Pileup[] pileups) {
		sb.append(SEP);
		sb.append(strand.character());

		char[] bases = Parameters.getInstance().getBaseConfig().getBases();
		byte minBasq = Parameters.getInstance().getMinBASQ();
		for (Pileup pileup : pileups) {

			sb.append(SEP);
			sb.append(pileup.getCoverage());
			sb.append(SEP);
			
			for (int baseI : pileup.getAlleles()) {
				// print bases 
				for (int i = 0; i < pileup.getCounts().getBaseCount()[baseI]; ++i) {
					sb.append(bases[baseI]);
				}
			}

			sb.append(SEP);

			// print quals
			for (int baseI : pileup.getAlleles()) {
				for (byte qual = minBasq; qual < Phred2Prob.MAX_Q; ++qual) {
					int count = pileup.getCounts().getQualCount(baseI, qual);
					if (count > 0) {
						// repeat count times
						for (int i = 0; i < count; ++i) {
							sb.append(SAMUtils.phredToFastq(qual));
						}
					}
				}
			}
		}
	}
	
	@Override
	public char getCOMMENT() {
		return COMMENT;
	}

	@Override
	public char getEMPTY() {
		return EMPTY;
	}

	@Override
	public char getSEP() {
		return SEP;
	}

	@Override
	public char getSEP2() {
		return SEP2;
	}

}