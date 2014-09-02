package accusa2.method;

import java.util.HashMap;
import java.util.Map;

import accusa2.cli.options.PileupBuilderOption;
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
import accusa2.cli.parameters.CLI;
import accusa2.cli.parameters.Parameters;
import accusa2.io.format.output.AbstractOutputFormat;
import accusa2.io.format.output.PileupFormat;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.dispatcher.MpileupWorkerDispatcher;
import accusa2.process.parallelpileup.worker.MpileupWorker;
import accusa2.util.CoordinateProvider;

public class TwoSamplePileupFactory extends AbstractMethodFactory {

	private static MpileupWorkerDispatcher instance;
	
	private TwoSamplePileupParameters parameters;
	
	public TwoSamplePileupFactory() {
		super("pileup", "SAMtools like mpileup");
	}

	public void initACOptions() {
		acOptions.add(new PathnameOption(parameters, '1'));
		acOptions.add(new PathnameOption(parameters, '2'));

		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		
		if(getOuptutFormats().size() == 1 ) {
			Character[] a = getOuptutFormats().keySet().toArray(new Character[1]);
			parameters.setResultFormat(getOuptutFormats().get(a[0]));
		} else {
			acOptions.add(new FormatOption(parameters, getOuptutFormats()));
		}
		
		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new MinCoverageOption(parameters));
		acOptions.add(new MinBASQOption(parameters));
		acOptions.add(new MinMAPQOption(parameters));
		acOptions.add(new MaxDepthOption(parameters));
		acOptions.add(new MaxThreadOption(parameters));
		acOptions.add(new FilterFlagOption(parameters));
		acOptions.add(new RetainFlagOption(parameters));
		
		acOptions.add(new WindowSizeOption(parameters));
		
		acOptions.add(new PileupBuilderOption(parameters));

		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(parameters, CLI.getSingleton()));
		acOptions.add(new VersionOption(parameters, CLI.getSingleton()));
	}

	@Override
	public AbstractParallelPileupWorkerDispatcher<MpileupWorker> getInstance(CoordinateProvider coordinateProvider, Parameters parameters) {
		if(instance == null) {
			instance = new MpileupWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}

	public Map<Character, AbstractOutputFormat> getOuptutFormats() {
		Map<Character, AbstractOutputFormat> outputFormats = new HashMap<Character, AbstractOutputFormat>();

		AbstractOutputFormat outputFormat = new PileupFormat();
		outputFormats.put(outputFormat.getC(), outputFormat);

		return outputFormats;
	}

}
