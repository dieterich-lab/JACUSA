package accusa2.method;

import java.util.HashMap;
import java.util.Map;

import accusa2.cli.CLI;
import accusa2.cli.Parameters;
import accusa2.cli.options.PileupBuilderOption;
import accusa2.cli.options.ConsiderBasesOption;
import accusa2.cli.options.DebugOption;
import accusa2.cli.options.HelpOption;
import accusa2.cli.options.MaxDepthOption;
import accusa2.cli.options.MaxThreadOption;
import accusa2.cli.options.MinBASQOption;
import accusa2.cli.options.MinCoverageOption;
import accusa2.cli.options.MinMAPQOption;
import accusa2.cli.options.PathnameOption;
import accusa2.cli.options.ResultFileOption;
import accusa2.cli.options.ResultFormatOption;
import accusa2.cli.options.RetainFlagOption;
import accusa2.cli.options.BED_CoordinatesOption;
import accusa2.cli.options.VersionOption;
import accusa2.cli.options.WindowSizeOption;
import accusa2.cli.options.filter.FilterFlagOption;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.format.PileupFormat;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.dispatcher.MpileupParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.worker.MpileupParallelPileupWorker;
import accusa2.util.CoordinateProvider;

public class PileupFactory extends AbstractMethodFactory {

	private static MpileupParallelPileupWorkerDispatcher instance;
	
	public PileupFactory() {
		super("pileup", "SAMtools like mpileup");
	}

	public void initACOptions() {
		acOptions.add(new PathnameOption(parameters, '1'));
		acOptions.add(new PathnameOption(parameters, '2'));

		acOptions.add(new BED_CoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		
		if(getResultFormats().size() == 1 ) {
			Character[] a = getResultFormats().keySet().toArray(new Character[1]);
			parameters.setResultFormat(getResultFormats().get(a[0]));
		} else {
			acOptions.add(new ResultFormatOption(parameters, getResultFormats()));
		}
		
		acOptions.add(new ConsiderBasesOption(parameters));
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
	public AbstractParallelPileupWorkerDispatcher<MpileupParallelPileupWorker> getInstance(CoordinateProvider coordinateProvider, Parameters parameters) {
		if(instance == null) {
			instance = new MpileupParallelPileupWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}

	public Map<Character, AbstractResultFormat> getResultFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new PileupFormat();
		resultFormats.put(resultFormat.getC(), resultFormat);

		return resultFormats;
	}

}
