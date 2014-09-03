package accusa2.method;

import java.util.HashMap;
import java.util.Map;

import accusa2.cli.options.BaseConfigOption;
import accusa2.cli.options.DebugOption;
import accusa2.cli.options.HelpOption;
import accusa2.cli.options.MaxDepthOption;
import accusa2.cli.options.MaxThreadOption;
import accusa2.cli.options.MinBASQOption;
import accusa2.cli.options.MinCoverageOption;
import accusa2.cli.options.MinMAPQOption;
import accusa2.cli.options.PathnameOption;
import accusa2.cli.options.ResultFileOption;
import accusa2.cli.options.FormatOption;
import accusa2.cli.options.RetainFlagOption;
import accusa2.cli.options.BedCoordinatesOption;
import accusa2.cli.options.VersionOption;
import accusa2.cli.options.WindowSizeOption;
import accusa2.cli.options.filter.FilterFlagOption;
import accusa2.cli.options.pileupbuilder.AbstractPileupBuilderOption;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.CLI;
import accusa2.cli.parameters.SampleParameters;
import accusa2.cli.parameters.TwoSamplePileupParameters;
import accusa2.io.format.output.AbstractOutputFormat;
import accusa2.io.format.output.PileupFormat;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.dispatcher.MpileupWorkerDispatcher;
import accusa2.process.parallelpileup.worker.AbstractParallelPileupWorker;
import accusa2.process.parallelpileup.worker.MpileupWorker;
import accusa2.util.CoordinateProvider;

public class TwoSamplePileupFactory extends AbstractMethodFactory {

	private static MpileupWorkerDispatcher instance;
	
	private TwoSamplePileupParameters parameters;
	
	public TwoSamplePileupFactory() {
		super("pileup", "SAMtools like mpileup");
		
		parameters = new TwoSamplePileupParameters();
	}

	public void initACOptions() {
		SampleParameters sampleA = parameters.getSampleA();
		acOptions.add(new PathnameOption('A', sampleA));

		SampleParameters sampleB = parameters.getSampleB();
		acOptions.add(new PathnameOption('B', sampleB));

		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		
		if(getOuptutFormats().size() == 1 ) {
			Character[] a = getOuptutFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getOuptutFormats().get(a[0]));
		} else {
			acOptions.add(new FormatOption<AbstractOutputFormat>(parameters, getOuptutFormats()));
		}
		
		acOptions.add(new BaseConfigOption(parameters));
			acOptions.add(new WindowSizeOption(parameters));
	
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
	}

	public Map<Character, AbstractOutputFormat> getOuptutFormats() {
		Map<Character, AbstractOutputFormat> outputFormats = new HashMap<Character, AbstractOutputFormat>();

		AbstractOutputFormat outputFormat = new PileupFormat(parameters.getBaseConfig());
		outputFormats.put(outputFormat.getC(), outputFormat);

		return outputFormats;
	}

	@Override
	public AbstractParallelPileupWorkerDispatcher<? extends AbstractParallelPileupWorker> getInstance(
			CoordinateProvider coordinateProvider, AbstractParameters parameters) {
		if(instance == null) {
			instance = new MpileupWorkerDispatcher(coordinateProvider, parameters);
		}

		return instance;;
	}

}
