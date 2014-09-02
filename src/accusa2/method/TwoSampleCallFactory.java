package accusa2.method;


import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

import accusa2.cli.options.PileupBuilderOption;
import accusa2.cli.options.BaseConfigOption;
import accusa2.cli.options.DebugOption;
import accusa2.cli.options.StatisticFilterOption;
import accusa2.cli.options.HelpOption;
import accusa2.cli.options.MaxDepthOption;
import accusa2.cli.options.MaxThreadOption;
import accusa2.cli.options.MinBASQOption;
import accusa2.cli.options.MinCoverageOption;
import accusa2.cli.options.MinMAPQOption;
import accusa2.cli.options.PathnameOption;
//import accusa2.cli.options.PermutationsOption;
import accusa2.cli.options.FilterConfigOption;
import accusa2.cli.options.StatisticCalculatorOption;
import accusa2.cli.options.ResultFileOption;
import accusa2.cli.options.FormatOption;
import accusa2.cli.options.RetainFlagOption;
import accusa2.cli.options.BedCoordinatesOption;
import accusa2.cli.options.VersionOption;
import accusa2.cli.options.WindowSizeOption;
import accusa2.cli.options.filter.FilterFlagOption;
import accusa2.cli.parameters.CLI;
import accusa2.cli.parameters.Parameters;
import accusa2.cli.parameters.SampleParameters;
import accusa2.cli.parameters.TwoSampleCallParameters;
import accusa2.filter.FilterConfig;
//import accusa2.cli.options.filter.FilterNHsamTagOption;
//import accusa2.cli.options.filter.FilterNMsamTagOption;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.filter.factory.HomopolymerFilterFactory;
import accusa2.filter.factory.HomozygousFilterFactory;
//import accusa2.filter.factory.PolymorphismPileupFilterFactory;
import accusa2.filter.factory.DistanceFilterFactory;
import accusa2.filter.factory.PolymorphismPileupFilterFactory;
import accusa2.filter.factory.RareEventFilterFactory;
//import accusa2.filter.factory.RareEventFilterFactory;
import accusa2.io.format.output.PileupResultFormat;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.io.format.result.DefaultResultFormat;
import accusa2.method.statistic.LR2Statistic;
import accusa2.method.statistic.LRStatistic;
import accusa2.method.statistic.MethodOfMomentsStatistic;
import accusa2.method.statistic.MixtureDirichletStatistic;
import accusa2.method.statistic.NumericalStatistic;
import accusa2.method.statistic.WeightedMethodOfMomentsStatistic;
//import accusa2.method.statistic.MinimalCoverageStatistic;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.process.parallelpileup.dispatcher.ParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.worker.ParallelPileupWorker;
import accusa2.util.CoordinateProvider;

public class TwoSampleCallFactory extends AbstractMethodFactory {

	private static ParallelPileupWorkerDispatcher instance;
	
	private TwoSampleCallParameters parameters;
	
	public TwoSampleCallFactory() {
		super("call", "Call variants");

	}

	public void initACOptions() {
		SampleParameters sampleA = parameters.getSampleA();
		acOptions.add(new PathnameOption(sampleA, 'A'));
		
		SampleParameters sampleB = parameters.getSampleB();
		acOptions.add(new PathnameOption(sampleA, 'B'));

		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		if(getResultFormats().size() == 1 ) {
			Character[] a = getResultFormats().keySet().toArray(new Character[1]);
			parameters.setResultFormat(getResultFormats().get(a[0]));
		} else {
			acOptions.add(new FormatOption(parameters, getResultFormats()));
		}

		acOptions.add(new MaxThreadOption(parameters));
		acOptions.add(new WindowSizeOption(parameters));

		/* TODO set for each sample 
		acOptions.add(new MinCoverageOption(parameters));
		acOptions.add(new MinBASQOption(parameters));
		acOptions.add(new MinMAPQOption(parameters));
		acOptions.add(new MaxDepthOption(parameters));
		acOptions.add(new FilterFlagOption(parameters));
		acOptions.add(new RetainFlagOption(parameters));
		*/
		
		acOptions.add(new FormatOption(parameters, getResultFormats()));

		if (getStatistics().size() == 1 ) {
			String[] a = getStatistics().keySet().toArray(new String[1]);
			parameters.setStatistic(getStatistics().get(a[0]));
		} else {
			acOptions.add(new StatisticCalculatorOption(parameters, getStatistics()));
		}

		acOptions.add(new FilterConfigOption(parameters, getFilterFactories()));

		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new StatisticFilterOption(parameters));
		//acOptions.add(new PermutationsOption(parameters));
		acOptions.add(new PileupBuilderOption(parameters));
		
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(parameters, CLI.getSingleton()));
		acOptions.add(new VersionOption(parameters, CLI.getSingleton()));
		
		//acOptions.add(new FilterNHsamTagOption(parameters));
		//acOptions.add(new FilterNMsamTagOption(parameters));
	}

	@Override
	public AbstractParallelPileupWorkerDispatcher<ParallelPileupWorker> getInstance(CoordinateProvider coordinateProvider, Parameters parameters) {
		if(instance == null) {
			instance = new ParallelPileupWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}

	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = null;

		statistic = new LRStatistic(parameters);
		statistics.put(statistic.getName(), statistic);
	
		statistic = new LR2Statistic(parameters);
		statistics.put(statistic.getName(), statistic);

		statistic = new MethodOfMomentsStatistic(parameters);
		statistics.put(statistic.getName(), statistic);
		
		statistic = new WeightedMethodOfMomentsStatistic(parameters);
		statistics.put(statistic.getName(), statistic);
		
		statistic = new MixtureDirichletStatistic(parameters);
		statistics.put(statistic.getName(), statistic);
		
		statistic = new NumericalStatistic(parameters);
		statistics.put(statistic.getName(), statistic);
		
		return statistics;
	}

	public Map<Character, AbstractFilterFactory> getFilterFactories() {
		Map<Character, AbstractFilterFactory> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory>();

		FilterConfig filterConfig = parameters.getStatisticParameters().getFilterConfig();
		AbstractFilterFactory[] filters = new AbstractFilterFactory[] {
				new DistanceFilterFactory(),
				new HomozygousFilterFactory(),
				new HomopolymerFilterFactory(),
				new RareEventFilterFactory(),
				new PolymorphismPileupFilterFactory()
		};
		for (AbstractFilterFactory filter : filters) {
			filter.setFilterConfig(filterConfig);
			abstractPileupFilters.put(filter.getC(), filter);
		}

		return abstractPileupFilters;
	}

	public Map<Character, AbstractResultFormat> getResultFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new DefaultResultFormat(parameters);
		resultFormats.put(resultFormat.getC(), resultFormat);

		resultFormat = new PileupResultFormat(parameters);
		resultFormats.put(resultFormat.getC(), resultFormat);

		return resultFormats;
	}

}
