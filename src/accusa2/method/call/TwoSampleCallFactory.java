package accusa2.method.call;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import net.sf.samtools.SAMSequenceRecord;
import accusa2.cli.options.BaseConfigOption;
import accusa2.cli.options.DebugOption;
// import accusa2.cli.options.EstimateParametersOption;
import accusa2.cli.options.MaxDepthOption;
import accusa2.cli.options.MinBASQOption;
import accusa2.cli.options.MinCoverageOption;
import accusa2.cli.options.MinMAPQOption;
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
import accusa2.cli.options.filter.FilterFlagOption;
import accusa2.cli.options.pileupbuilder.TwoSamplePileupBuilderOption;
import accusa2.cli.options.sample.MaxDepthSampleOption;
import accusa2.cli.options.sample.MinBASQSampleOption;
import accusa2.cli.options.sample.MinCoverageSampleOption;
import accusa2.cli.options.sample.MinMAPQSampleOption;
import accusa2.cli.parameters.AbstractParameters;
import accusa2.cli.parameters.CLI;
import accusa2.cli.parameters.SampleParameters;
import accusa2.cli.parameters.TwoSampleCallParameters;
import accusa2.estimate.AbstractEstimateParameters;
import accusa2.estimate.BayesEstimateParameters;
import accusa2.estimate.coverage.CoverageEstimateParameters;
//import accusa2.cli.options.filter.FilterNHsamTagOption;
//import accusa2.cli.options.filter.FilterNMsamTagOption;
import accusa2.filter.factory.AbstractFilterFactory;
import accusa2.filter.factory.HomopolymerFilterFactory;
import accusa2.filter.factory.HomozygousFilterFactory;
import accusa2.filter.factory.DistanceFilterFactory;
import accusa2.filter.factory.PolymorphismPileupFilterFactory;
import accusa2.filter.factory.RareEventFilterFactory;
import accusa2.io.format.output.PileupResultFormat;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.io.format.result.DefaultResultFormat;
import accusa2.method.AbstractMethodFactory;
import accusa2.method.call.statistic.ACCUSA2Statistic;
import accusa2.method.call.statistic.DirichletMultinomialMLEStatistic;
import accusa2.method.call.statistic.DirichletMOMsStatistic;
import accusa2.method.call.statistic.StatisticCalculator;
import accusa2.method.call.statistic.lr.LR2Statistic;
import accusa2.method.call.statistic.lr.LRStatistic;
//import accusa2.method.call.statistic.WeightedMethodOfMomentsStatistic;
import accusa2.process.parallelpileup.dispatcher.call.TwoSampleCallWorkerDispatcher;
import accusa2.process.phred2prob.Phred2Prob;
import accusa2.util.CoordinateProvider;
import accusa2.util.SAMCoordinateProvider;

public class TwoSampleCallFactory extends AbstractMethodFactory {

	public static final char sample1 = 'A';
	public static final char sample2 = 'B';
	private TwoSampleCallParameters parameters;

	private static TwoSampleCallWorkerDispatcher instance;

	public TwoSampleCallFactory() {
		super("call-2", "Call variants - two samples");
		parameters = new TwoSampleCallParameters();
	}

	public void initACOptions() {
		SampleParameters sampleA = parameters.getSampleA();
		acOptions.add(new PathnameOption(sample1, sampleA ));
		acOptions.add(new MinMAPQSampleOption(sample1, sampleA));
		acOptions.add(new MinBASQSampleOption(sample1, sampleA));
		acOptions.add(new MinCoverageSampleOption(sample1, sampleA));
		acOptions.add(new MaxDepthSampleOption(sample1, sampleA));
				
		SampleParameters sampleB = parameters.getSampleB();
		acOptions.add(new PathnameOption(sample2, sampleB));
		acOptions.add(new MinMAPQSampleOption(sample2, sampleB));
		acOptions.add(new MinBASQSampleOption(sample2, sampleB));
		acOptions.add(new MinCoverageSampleOption(sample2, sampleB));
		acOptions.add(new MaxDepthSampleOption(sample2, sampleB));
		
		// global settings
		acOptions.add(new MinMAPQOption(sampleA, sampleB));
		acOptions.add(new MinBASQOption(sampleA, sampleB));
		acOptions.add(new MinCoverageOption(sampleA, sampleB));
		acOptions.add(new MaxDepthOption(sampleA, sampleB));
		acOptions.add(new FilterFlagOption(sampleA, sampleB));

		acOptions.add(new TwoSamplePileupBuilderOption(sampleA, sampleB));
		
		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		if(getResultFormats().size() == 1 ) {
			Character[] a = getResultFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getResultFormats().get(a[0]));
		} else {
			acOptions.add(new FormatOption<AbstractResultFormat>(parameters, getResultFormats()));
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
		//acOptions.add(new PermutationsOption(parameters));

		//acOptions.add(new EstimateParametersOption(parameters.getStatisticParameters(), getEstimators()));
		
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));

		//acOptions.add(new FilterNHsamTagOption(parameters));
		//acOptions.add(new FilterNMsamTagOption(parameters));
	}

	@Override
	public TwoSampleCallWorkerDispatcher getInstance(CoordinateProvider coordinateProvider) throws IOException {
		if(instance == null) {
			instance = new TwoSampleCallWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}

	public Map<String, AbstractEstimateParameters> getEstimators() {
		Map<String, AbstractEstimateParameters> estimators = new TreeMap<String, AbstractEstimateParameters>();

		int baseN = parameters.getBaseConfig().getBases().length;
		Phred2Prob phred2Prob = Phred2Prob.getInstance(baseN);
		double [] alpha = new double[baseN];
		Arrays.fill(alpha, 0.0); // 1.0/baseN

		AbstractEstimateParameters estimator = null;

		estimator = new BayesEstimateParameters(alpha, phred2Prob);
		estimators.put(estimator.getName(), estimator);

		estimator = new CoverageEstimateParameters(alpha, phred2Prob);
		estimators.put(estimator.getName(), estimator);

		return estimators;
	}
	
	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = null;

		statistic = new ACCUSA2Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		
		statistic = new LRStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
	
		statistic = new LR2Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMOMsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		
		//statistic = new WeightedMethodOfMomentsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMultinomialMLEStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		return statistics;
	}

	public Map<Character, AbstractFilterFactory> getFilterFactories() {
		Map<Character, AbstractFilterFactory> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory>();

		AbstractFilterFactory[] filters = new AbstractFilterFactory[] {
				new DistanceFilterFactory(parameters),
				new HomozygousFilterFactory(),
				new HomopolymerFilterFactory(parameters),
				new RareEventFilterFactory(parameters),
				new PolymorphismPileupFilterFactory()
		};
		for (AbstractFilterFactory filter : filters) {
			abstractPileupFilters.put(filter.getC(), filter);
		}

		return abstractPileupFilters;
	}

	public Map<Character, AbstractResultFormat> getResultFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new DefaultResultFormat(parameters.getBaseConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		resultFormat = new PileupResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		return resultFormats;
	}

	@Override
	public void initCoordinateProvider() throws Exception {
		String[] pathnamesA = parameters.getSampleA().getPathnames();
		String[] pathnames2 = parameters.getSampleB().getPathnames();
		List<SAMSequenceRecord> records = getSAMSequenceRecords(pathnamesA, pathnames2);
		coordinateProvider = new SAMCoordinateProvider(records);
	}

	@Override
	public AbstractParameters getParameters() {
		return parameters;
	}
	
}