package accusa2.method.call;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import net.sf.samtools.SAMSequenceRecord;
import accusa2.cli.options.BaseConfigOption;
import accusa2.cli.options.DebugOption;
import accusa2.cli.options.PermutationsOption;
import accusa2.cli.options.StatisticFilterOption;
import accusa2.cli.options.HelpOption;
import accusa2.cli.options.MaxThreadOption;
import accusa2.cli.options.PathnameOption;
//import accusa2.cli.options.PermutationsOption;
import accusa2.cli.options.FilterConfigOption;
import accusa2.cli.options.StatisticCalculatorOption;
import accusa2.cli.options.ResultFileOption;
import accusa2.cli.options.FormatOption;
import accusa2.cli.options.BedCoordinatesOption;
import accusa2.cli.options.VersionOption;
import accusa2.cli.options.WindowSizeOption;
import accusa2.cli.options.filter.FilterNHsamTagOption;
import accusa2.cli.options.filter.FilterNMsamTagOption;
import accusa2.cli.options.pileupbuilder.OneSamplePileupBuilderOption;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.CLI;
import accusa2.cli.parameters.OneSampleCallParameters;
import accusa2.cli.parameters.SampleParameters;
//import accusa2.cli.options.filter.FilterNHsamTagOption;
//import accusa2.cli.options.filter.FilterNMsamTagOption;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.filter.factory.HomopolymerFilterFactory;
import accusa2.filter.factory.HomozygousFilterFactory;
import accusa2.filter.factory.PolymorphismPileupFilterFactory;
import accusa2.filter.factory.DistanceFilterFactory;
import accusa2.filter.factory.RareEventFilterFactory;
import accusa2.io.format.output.PileupResultFormat;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.io.format.result.DefaultResultFormat;
import accusa2.method.AbstractMethodFactory;
import accusa2.method.call.statistic.LR2Statistic;
import accusa2.method.call.statistic.LRStatistic;
import accusa2.method.call.statistic.MethodOfMomentsStatistic;
import accusa2.method.call.statistic.MixtureDirichletStatistic;
import accusa2.method.call.statistic.NumericalStatistic;
import accusa2.method.call.statistic.StatisticCalculator;
import accusa2.method.call.statistic.WeightedMethodOfMomentsStatistic;
import accusa2.process.parallelpileup.dispatcher.call.OneSampleCallWorkerDispatcher;
import accusa2.util.CoordinateProvider;
import accusa2.util.SAMCoordinateProvider;

public class OneSampleCallFactory extends AbstractMethodFactory {

	private OneSampleCallParameters parameters = new OneSampleCallParameters();
	
	private static OneSampleCallWorkerDispatcher instance;

	public OneSampleCallFactory() {
		super("call-1", "Call variants - one sample");
	}

	public void initACOptions() {
		SampleParameters sampleA = parameters.getSampleA();
		acOptions.add(new PathnameOption(TwoSampleCallFactory.sample1, sampleA));
		acOptions.add(new OneSamplePileupBuilderOption(sampleA));
		
		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		if(getFormats().size() == 1 ) {
			Character[] a = getFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getFormats().get(a[0]));
		} else {
			acOptions.add(new FormatOption<AbstractResultFormat>(parameters, getFormats()));
		}

		acOptions.add(new MaxThreadOption(parameters));
		acOptions.add(new WindowSizeOption(parameters));

		if (getStatistics().size() == 1 ) {
			String[] a = getStatistics().keySet().toArray(new String[1]);
			parameters.getStatisticParameters().setStatisticCalculator(getStatistics().get(a[0]));
		} else {
			acOptions.add(new StatisticCalculatorOption(parameters.getStatisticParameters(), getStatistics()));
		}

		acOptions.add(new FilterConfigOption(parameters, getFilterFactories()));

		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new StatisticFilterOption(parameters.getStatisticParameters()));
		acOptions.add(new PermutationsOption(parameters.getStatisticParameters()));
		
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
		
		acOptions.add(new FilterNHsamTagOption(parameters.getSampleA()));
		acOptions.add(new FilterNMsamTagOption(parameters.getSampleA()));
	}

	@Override
	public OneSampleCallWorkerDispatcher getInstance(CoordinateProvider coordinateProvider) throws IOException {
		if(instance == null) {
			instance = new OneSampleCallWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}

	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = null;

		statistic = new LRStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new LR2Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new MethodOfMomentsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new WeightedMethodOfMomentsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new MixtureDirichletStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new NumericalStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		return statistics;
	}

	public Map<Character, AbstractFilterFactory> getFilterFactories() {
		Map<Character, AbstractFilterFactory> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory>();

		AbstractFilterFactory[] filters = new AbstractFilterFactory[] {
				new DistanceFilterFactory(parameters),
				new HomozygousFilterFactory(),
				new HomopolymerFilterFactory(parameters),
				new RareEventFilterFactory(),
				new PolymorphismPileupFilterFactory()
		};
		for (AbstractFilterFactory filter : filters) {
			abstractPileupFilters.put(filter.getC(), filter);
		}

		return abstractPileupFilters;
	}

	public Map<Character, AbstractResultFormat> getFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new DefaultResultFormat(parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		resultFormat = new PileupResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		return resultFormats;
	}

	@Override
	public void initCoordinateProvider() throws Exception {
		String[] pathnames = parameters.getSampleA().getPathnames();

		List<SAMSequenceRecord> records = getSAMSequenceRecords(pathnames);
		coordinateProvider = new SAMCoordinateProvider(records);
	}

	@Override
	public AbstractParameters getParameters() {
		return parameters;
	}
	
}
