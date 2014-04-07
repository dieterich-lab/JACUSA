package accusa2.method;


import java.util.HashMap;
import java.util.TreeMap;
import java.util.Map;

import accusa2.cli.CLI;
import accusa2.cli.Parameters;
import accusa2.cli.options.PileupBuilderOption;
import accusa2.cli.options.ConsiderBasesOption;
import accusa2.cli.options.DebugOption;
import accusa2.cli.options.FalseDiscoveryRateOption;
import accusa2.cli.options.HelpOption;
import accusa2.cli.options.MaxDepthOption;
import accusa2.cli.options.MaxThreadOption;
import accusa2.cli.options.MinBASQOption;
import accusa2.cli.options.MinCoverageOption;
import accusa2.cli.options.MinMAPQOption;
import accusa2.cli.options.PathnameOption;
//import accusa2.cli.options.PermutationsOption;
import accusa2.cli.options.FilterOption;
import accusa2.cli.options.StatisticOption;
import accusa2.cli.options.ResultFileOption;
import accusa2.cli.options.ResultFormatOption;
import accusa2.cli.options.RetainFlagOption;
import accusa2.cli.options.BED_CoordinatesOption;
import accusa2.cli.options.VersionOption;
import accusa2.cli.options.WindowSizeOption;
import accusa2.cli.options.filter.FilterFlagOption;
//import accusa2.cli.options.filter.FilterNHsamTagOption;
//import accusa2.cli.options.filter.FilterNMsamTagOption;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.filter.factory.HomopolymerFilterFactory;
import accusa2.filter.factory.HomozygousFilterFactory;
//import accusa2.filter.factory.PolymorphismPileupFilterFactory;
import accusa2.filter.factory.DistanceFilterFactory;
import accusa2.filter.factory.RareEventFilterFactory;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.format.DefaultResultFormat;
import accusa2.io.format.PileupResultFormat;
import accusa2.method.statistic.CombinedLRStatistic;
import accusa2.method.statistic.CombinedStatistic;
import accusa2.method.statistic.DefaultStatistic;
import accusa2.method.statistic.LR2Statistic;
import accusa2.method.statistic.LRStatistic;
import accusa2.method.statistic.PooledStatistic;
//import accusa2.method.statistic.MinimalCoverageStatistic;
import accusa2.method.statistic.StatisticCalculator;
import accusa2.process.parallelpileup.dispatcher.ACCUSA25_ParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.dispatcher.AbstractParallelPileupWorkerDispatcher;
import accusa2.process.parallelpileup.worker.ACCUSA25_ParallelPileupWorker;
import accusa2.util.CoordinateProvider;

public class ACCUSA25Factory extends AbstractMethodFactory {

	private static ACCUSA25_ParallelPileupWorkerDispatcher instance;
	
	public ACCUSA25Factory() {
		super("call", "Call variants");
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

		acOptions.add(new MaxThreadOption(parameters));
		acOptions.add(new WindowSizeOption(parameters));

		acOptions.add(new MinCoverageOption(parameters));
		acOptions.add(new MinBASQOption(parameters));
		acOptions.add(new MinMAPQOption(parameters));
		acOptions.add(new MaxDepthOption(parameters));
		
		acOptions.add(new ResultFormatOption(parameters, getResultFormats()));
		
		acOptions.add(new FilterFlagOption(parameters));
		acOptions.add(new RetainFlagOption(parameters));

		if(getStatistics().size() == 1 ) {
			String[] a = getStatistics().keySet().toArray(new String[1]);
			parameters.setStatistic(getStatistics().get(a[0]));
		} else {
			acOptions.add(new StatisticOption(parameters, getStatistics()));
		}

		acOptions.add(new FilterOption(parameters, getFilterFactories()));

		//acOptions.add(new ConsiderBasesOption(parameters));
		acOptions.add(new FalseDiscoveryRateOption(parameters));
		//acOptions.add(new PermutationsOption(parameters));
		acOptions.add(new PileupBuilderOption(parameters));
		
		//acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(parameters, CLI.getSingleton()));
		acOptions.add(new VersionOption(parameters, CLI.getSingleton()));
		
		//acOptions.add(new FilterNHsamTagOption(parameters));
		//acOptions.add(new FilterNMsamTagOption(parameters));
		
	}

	@Override
	public AbstractParallelPileupWorkerDispatcher<ACCUSA25_ParallelPileupWorker> getInstance(CoordinateProvider coordinateProvider, Parameters parameters) {
		if(instance == null) {
			instance = new ACCUSA25_ParallelPileupWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}

	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = new DefaultStatistic(parameters);
		//StatisticCalculator ho_he = statistic;
		//statistics.put(statistic.getName(), statistic);
		
		
		//statistic = new PooledStatistic(parameters);
		//StatisticCalculator he_he = statistic;
		//statistics.put(statistic.getName(), statistic);

		/*
		statistic = new MinimalCoverageStatistic();
		statistics.put(statistic.getName(), statistic);
		*/

		//statistic = new CombinedStatistic(parameters,
		//		ho_he,
		//		he_he,
		//		"combined", 
		//		"default(ho:he) + pooled(he:he)");
		//statistics.put(statistic.getName(), statistic);

		//statistic = new LRStatistic(parameters);
		//ho_he = statistic;
		//statistics.put(statistic.getName(), statistic);
	
		statistic = new LR2Statistic(parameters);
		//he_he = statistic;
		statistics.put(statistic.getName(), statistic);

		//statistic = new CombinedLRStatistic(parameters,
		//		ho_he,
		//		he_he,
		//		"combinedLR", 
		//		"lr(ho:he) + lr2(he:he)");
		//statistics.put(statistic.getName(), statistic);

		return statistics;
	}

	public Map<Character, AbstractFilterFactory> getFilterFactories() {
		Map<Character, AbstractFilterFactory> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory>();

		AbstractFilterFactory abstractFilterFactory = null;

		abstractFilterFactory = new DistanceFilterFactory();
		abstractFilterFactory.setParameters(parameters);
		abstractPileupFilters.put(abstractFilterFactory.getC(), abstractFilterFactory);

		abstractFilterFactory = new HomozygousFilterFactory();
		abstractFilterFactory.setParameters(parameters);
		abstractPileupFilters.put(abstractFilterFactory.getC(), abstractFilterFactory);

		abstractFilterFactory = new HomopolymerFilterFactory();
		abstractFilterFactory.setParameters(parameters);
		abstractPileupFilters.put(abstractFilterFactory.getC(), abstractFilterFactory);
		/*
		abstractFilterFactory = new RareEventFilterFactory();
		abstractFilterFactory.setParameters(parameters);
		abstractPileupFilters.put(abstractFilterFactory.getC(), abstractFilterFactory);
		*/
		/*
		abstractFilterFactory = new PolymorphismPileupFilterFactory();
		abstractFilterFactory.setParameters(parameters);
		abstractPileupFilters.put(abstractFilterFactory.getC(), abstractFilterFactory);
		*/

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
