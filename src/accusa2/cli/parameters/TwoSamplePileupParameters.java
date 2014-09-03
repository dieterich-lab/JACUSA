package accusa2.cli.parameters;

public class TwoSamplePileupParameters extends AbstractParameters implements hasSampleB {

	private SampleParameters sampleB;

	public TwoSamplePileupParameters() {
		super();

		sampleB				= new SampleParameters();
	}

	@Override
	public SampleParameters getSampleB() {
		return sampleB;
	}


}