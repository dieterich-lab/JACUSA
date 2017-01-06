package jacusa.cli.options.pileupbuilder;

import jacusa.cli.options.AbstractACOption;
import jacusa.pileup.builder.FRPairedEnd1PileupBuilderFactory;
import jacusa.pileup.builder.FRPairedEnd2PileupBuilderFactory;
import jacusa.pileup.builder.SingleEndStrandedPileupBuilderFactory;
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
		case SE_STRANDED:
			return new SingleEndStrandedPileupBuilderFactory();
		
		case UNSTRANDED:
			return new UnstrandedPileupBuilderFactory();
		
		case FR_FIRSTSTRAND:
			return new FRPairedEnd1PileupBuilderFactory();
		
		case FR_SECONDSTRAND:
			return new FRPairedEnd2PileupBuilderFactory();
			
		default:
			return null;
		}
	}

	public LibraryType parse(String s) {
		s = s.toUpperCase();

		// for compatibility with older versions 
		if (s.length() == 1) {
			switch(s.charAt(0)) {
			case 'S':
				return LibraryType.SE_STRANDED;
				
			case 'U':
				return LibraryType.UNSTRANDED;
			}	
		}

		s = s.replace("-", "_");
		
		switch(LibraryType.valueOf(s)) {
		case SE_STRANDED:
			return LibraryType.SE_STRANDED;

		case UNSTRANDED:
			return LibraryType.UNSTRANDED;
			
		case FR_FIRSTSTRAND:
			return LibraryType.FR_FIRSTSTRAND;
		
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
			case SE_STRANDED:
				desc = "Single End STRANDED library";
				break;
				
			case FR_FIRSTSTRAND:
				desc = "Paired End STRANDED library - first read";
				break;
				
			case FR_SECONDSTRAND:
				desc = "Paired End STRANDED library - second read";
				break;

			case UNSTRANDED:
				desc = "UNSTRANDED library";
				break;

			}
			
			sb.append(option);
			sb.append("\t");
			sb.append(desc);
			sb.append("\n");
		}
		
		return sb.toString();
	}

	public enum LibraryType {
		SE_STRANDED, 
		UNSTRANDED, 
		FR_FIRSTSTRAND, 
		FR_SECONDSTRAND
	}
	
}