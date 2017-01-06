package jacusa.cli.options.pileupbuilder;

import jacusa.cli.parameters.SampleParameters;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class TwoSamplePileupBuilderOption extends AbstractPileupBuilderOption {

	private SampleParameters parameters1;
	private SampleParameters parameters2;
	
	public TwoSamplePileupBuilderOption(SampleParameters parametersA, SampleParameters parametersB) {
		this.parameters1 = parametersA;
		this.parameters2 = parametersB;
	}

	@SuppressWarnings("static-access")
	@Override
	public Option getOption() {
		return OptionBuilder.withLongOpt(longOpt)
			.withArgName(longOpt.toUpperCase())
			.hasArg(true)
			.withDescription("Choose the library types and how parallel pileups are build for sample1(s1) and sample2(s2).\nFormat: s1,s2. \nPossible values for s1 and s2:\n" + getPossibleValues() + "\ndefault: " + LibraryType.UNSTRANDED + SEP + LibraryType.UNSTRANDED)
			.create(opt);
	}

	@Override
	public void process(CommandLine line) throws Exception {
		if (line.hasOption(opt)) {
	    	String s = line.getOptionValue(opt);
	    	String[] ss = s.split(Character.getName(SEP));

	    	StringBuilder sb = new StringBuilder();
	    	sb.append("Format: s1,s2. \n");
	    	sb.append("Possible values for s1 and s2:\n");
	    	sb.append(getPossibleValues());
	    	
	    	if (ss.length != 2) {
	    		throw new IllegalArgumentException(sb.toString());
	    	}
	    	
	    	LibraryType l1 = parse(ss[0]);
	    	LibraryType l2 = parse(ss[1]);
	    	
	    	if (l1 == null || l2 == null) {
	    		throw new IllegalArgumentException(sb.toString());
	    	}
	    	parameters1.setPileupBuilderFactory(buildPileupBuilderFactory(l1));
	    	parameters2.setPileupBuilderFactory(buildPileupBuilderFactory(l2));
	    }
	}

}