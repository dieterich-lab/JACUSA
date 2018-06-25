package jacusa.cli.options.pileupbuilder;

import jacusa.cli.options.AbstractACOption;
import jacusa.pileup.builder.RFPairedEnd1PileupBuilderFactory;
import jacusa.pileup.builder.FRPairedEnd2PileupBuilderFactory;
import jacusa.pileup.builder.PileupBuilderFactory;
import jacusa.pileup.builder.UnstrandedPileupBuilderFactory;

public abstract class AbstractPileupBuilderOption extends AbstractACOption {
	
	protected static final char SEP = ',';

	public AbstractPileupBuilderOption() {
		opt = "P";
		longOpt = "build-pileup";
	}

	protected PileupBuilderFactory buildPileupBuilderFactory(LibraryType libraryType) {
		switch(libraryType) {
		case UNSTRANDED:
			return new UnstrandedPileupBuilderFactory();
		
		case RF_FIRSTSTRAND:
			return new RFPairedEnd1PileupBuilderFactory();
		
		case FR_SECONDSTRAND:
			return new FRPairedEnd2PileupBuilderFactory();
			
		default:
			return null;
		}
	}

	public LibraryType parse(String s) {
		s = s.toUpperCase();

		// for compatibility with older versions 
		/* SE stranded now - flips the strand
		if (s.length() == 1) {
			switch(s.charAt(0)) {
			case 'S':
				return LibraryType.SE_STRANDED;
				
			case 'U':
				return LibraryType.UNSTRANDED;
			}	
		}
		*/

		s = s.replace("-", "_");
		
		switch(LibraryType.valueOf(s)) {

		case UNSTRANDED:
			return LibraryType.UNSTRANDED;
			
		case RF_FIRSTSTRAND:
			return LibraryType.RF_FIRSTSTRAND;
		
		case FR_SECONDSTRAND:
			return LibraryType.FR_SECONDSTRAND;
		}

		return null;
	}
	
	public String getPossibleValues() {
		StringBuilder sb = new StringBuilder();
		
		for (LibraryType l : LibraryType.values()) {
			String option = l.toString();
			option = option.replace("_", "-");
			String desc = "";

			switch (l) {
			case RF_FIRSTSTRAND:
				desc = "STRANDED library - first strand sequenced";
				break;
				
			case FR_SECONDSTRAND:
				desc = "STRANDED library - second strand sequenced";
				break;

			case UNSTRANDED:
				desc = "UNSTRANDED library";
				break;

			}
			
			sb.append(option);
			sb.append("\t\t");
			sb.append(desc);
			sb.append("\n");
		}
		
		return sb.toString();
	}

	public enum LibraryType {
		RF_FIRSTSTRAND, 
		FR_SECONDSTRAND,
		UNSTRANDED
	}
	
}