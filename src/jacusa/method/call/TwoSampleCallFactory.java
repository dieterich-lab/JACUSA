package jacusa.method.call;


import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.BaseConfigOption;
import jacusa.cli.options.BedCoordinatesOption;
//import jacusa.cli.options.DebugOption;
import jacusa.cli.options.FilterConfigOption;
import jacusa.cli.options.FilterModusOption;
import jacusa.cli.options.FormatOption;
import jacusa.cli.options.HelpOption;
import jacusa.cli.options.MaxDepthOption;
import jacusa.cli.options.MaxThreadOption;
import jacusa.cli.options.MinBASQOption;
import jacusa.cli.options.MinCoverageOption;
import jacusa.cli.options.MinMAPQOption;
import jacusa.cli.options.SAMPathnameArg;
import jacusa.cli.options.ResultFileOption;
import jacusa.cli.options.StatisticCalculatorOption;
import jacusa.cli.options.StatisticFilterOption;
import jacusa.cli.options.ThreadWindowSizeOption;
import jacusa.cli.options.VersionOption;
import jacusa.cli.options.WindowSizeOption;
import jacusa.cli.options.pileupbuilder.TwoSamplePileupBuilderOption;
import jacusa.cli.options.sample.filter.FilterFlagOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.filter.factory.AbstractFilterFactory;
//import jacusa.filter.factory.AssumeAlleleCountFilterFactors;
//import jacusa.filter.factory.BASQBiasFilterFactory;
import jacusa.filter.factory.DistanceFilterFactory;
//import jacusa.filter.factory.FDRFilterFactory;
import jacusa.filter.factory.HomopolymerFilterFactory;
import jacusa.filter.factory.HomozygousFilterFactory;
import jacusa.filter.factory.INDEL_DistanceFilterFactory;
import jacusa.filter.factory.MaxAlleleCountFilterFactors;
import jacusa.filter.factory.MinDifferenceFilterFactory;
//import jacusa.filter.factory.OutlierFilterFactory;
//import jacusa.filter.factory.OutlierFilterFactory;
import jacusa.filter.factory.ReadPositionDistanceFilterFactory;
import jacusa.filter.factory.SpliceSiteDistanceFilterFactory;
//import jacusa.filter.factory.ZeroCountFilterFactory;
//import jacusa.filter.factory.MAPQBiasFilterFactory;
//import jacusa.filter.factory.ReadPositionalBiasFilterFactory;
import jacusa.filter.factory.RareEventFilterFactory;
import jacusa.io.format.AbstractOutputFormat;
import jacusa.io.format.BED6ResultFormat;
// import jacusa.io.format.DebugResultFormat;
//import jacusa.io.format.DebugResultFormat;
//import jacusa.io.format.DefaultOutputFormat;
//import jacusa.io.format.PileupResultFormat;
import jacusa.io.format.VCF_ResultFormat;
//import jacusa.io.format.result.DebugResultFormat;
//import jacusa.io.format.result.VCF_ResultFormat;
import jacusa.method.AbstractMethodFactory;
import jacusa.method.call.statistic.ACCUSA2Statistic;
// import jacusa.method.call.statistic.ACCUSA2Statistic;
// import jacusa.method.call.statistic.ACCUSA2Statistic;
//import jacusa.method.call.statistic.ACCUSA2Statistic;
//import jacusa.method.call.statistic.DirichletBayesStatistic;
//import jacusa.method.call.statistic.DirichletStatistic;
//import jacusa.method.call.statistic.DirichletMOMsStatistic;
import jacusa.method.call.statistic.StatisticCalculator;
//import jacusa.method.call.statistic.dirmult.DirichletMultinomial;
//import jacusa.method.call.statistic.dirmult.DirichletMultinomialPooledError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialCompoundError;
//import jacusa.method.call.statistic.dirmult.DirichletMultinomialEstimatedError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialRobustCompoundError;
//import jacusa.method.call.statistic.lr.LR_SENS_Statistic;
//import jacusa.method.call.statistic.lr.LR_SPEC_Statistic;
//import jacusa.method.call.statistic.lr.LR_SENS_Statistic;
//import jacusa.method.call.statistic.lr.LR_SPEC_Statistic;
import jacusa.pileup.dispatcher.call.TwoSampleCallWorkerDispatcher;
import jacusa.util.coordinateprovider.CoordinateProvider;
import jacusa.util.coordinateprovider.SAMCoordinateProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import net.sf.samtools.SAMSequenceRecord;

public class TwoSampleCallFactory extends AbstractMethodFactory {

	private TwoSampleCallParameters parameters;

	private static TwoSampleCallWorkerDispatcher instance;

	public TwoSampleCallFactory() {
		super("call-2", "Call variants - two samples");
		parameters = new TwoSampleCallParameters();
	}
	
	public void initACOptions() {
		// sample specific setting
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();

		for (int sampleI = 1; sampleI <= 2; ++sampleI) {
			initSampleACOptions(sampleI, sample1);
			initSampleACOptions(sampleI, sample2);
		}
		SampleParameters[] samples = new SampleParameters[] {
			sample1, sample2
		};
		
		// global settings
		acOptions.add(new MinMAPQOption(samples));
		acOptions.add(new MinBASQOption(samples));
		acOptions.add(new MinCoverageOption(samples));
		acOptions.add(new MaxDepthOption(parameters));
		acOptions.add(new FilterFlagOption(samples));
		
		acOptions.add(new TwoSamplePileupBuilderOption(sample1, sample2));

		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		if (getResultFormats().size() == 1 ) {
			Character[] a = getResultFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getResultFormats().get(a[0]));
		} else {
			parameters.setFormat(getResultFormats().get(BED6ResultFormat.CHAR));
			acOptions.add(new FormatOption<AbstractOutputFormat>(parameters, getResultFormats()));
		}

		acOptions.add(new MaxThreadOption(parameters));
		acOptions.add(new WindowSizeOption(parameters));
		acOptions.add(new ThreadWindowSizeOption(parameters));

		if (getStatistics().size() == 1 ) {
			String[] a = getStatistics().keySet().toArray(new String[1]);
			parameters.getStatisticParameters().setStatisticCalculator(getStatistics().get(a[0]));
		} else {
			acOptions.add(new StatisticCalculatorOption(parameters.getStatisticParameters(), getStatistics()));
		}

		acOptions.add(new FilterModusOption(parameters));
		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new FilterConfigOption(parameters, getFilterFactories()));
		
		acOptions.add(new StatisticFilterOption(parameters.getStatisticParameters()));

		// 
		// acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
	}

	@Override
	public TwoSampleCallWorkerDispatcher getInstance(String[] pathnames1, String[] pathnames2, CoordinateProvider coordinateProvider) throws IOException {
		if(instance == null) {
			instance = new TwoSampleCallWorkerDispatcher(pathnames1, pathnames2, coordinateProvider, parameters);
		}
		return instance;
	}
	
	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = null;

		statistic = new DirichletMultinomialRobustCompoundError	(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put("DirMult", statistic);
		
		statistic = new ACCUSA2Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		
		// RC statistic = new LR_SPEC_Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		// RC statistics.put(statistic.getName(), statistic);
	
		// RC statistic = new LR_SENS_Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		// RC statistics.put(statistic.getName(), statistic);

		//statistic = new DirichletBayesStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);
		
		//statistic = new DirichletMOMsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		//statistic = new WeightedMethodOfMomentsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		//statistic = new DirichletStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		//statistic = new DirichletBayesLRStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		// RC statistic = new DirichletMultinomial(parameters.getBaseConfig(), parameters.getStatisticParameters());
		// RC statistics.put(statistic.getName(), statistic);

		//statistic = new DirichletMultinomialPooledError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);
		
		statistic = new DirichletMultinomialCompoundError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMultinomialRobustCompoundError	(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		//statistic = new DirichletMultinomialEstimatedError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);
		
		return statistics;
	}

	public Map<Character, AbstractFilterFactory<?>> getFilterFactories() {
		Map<Character, AbstractFilterFactory<?>> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory<?>>();

		AbstractFilterFactory<?>[] filterFactories = new AbstractFilterFactory[] {
//				new ReadPositionalBiasFilterFactory(parameters),
//				new BASQBiasFilterFactory(parameters),
//				new MAPQBiasFilterFactory(parameters),
//				new OutlierFilterFactory(parameters.getStatisticParameters()),
//				new ZeroCountFilterFactory(parameters.getStatisticParameters()),
				new DistanceFilterFactory(parameters),
				new INDEL_DistanceFilterFactory(parameters),
				new ReadPositionDistanceFilterFactory(parameters),
				new SpliceSiteDistanceFilterFactory(parameters),
				new HomozygousFilterFactory(parameters),
				new MaxAlleleCountFilterFactors(parameters),
				// RC new AssumeAlleleCountFilterFactors(),
				new HomopolymerFilterFactory(parameters),
				new RareEventFilterFactory(parameters),
				new MinDifferenceFilterFactory(parameters),
				// RC new OutlierFilterFactory(parameters.getStatisticParameters()),
				// RC new FDRFilterFactory(parameters.getStatisticParameters()),
		};
		for (AbstractFilterFactory<?> filterFactory : filterFactories) {
			abstractPileupFilters.put(filterFactory.getC(), filterFactory);
		}

		return abstractPileupFilters;
	}

	public Map<Character, AbstractOutputFormat> getResultFormats() {
		Map<Character, AbstractOutputFormat> resultFormats = new HashMap<Character, AbstractOutputFormat>();

		AbstractOutputFormat resultFormat = null;
		
		//AbstractOutputFormat resultFormat = new DefaultOutputFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		//resultFormats.put(resultFormat.getC(), resultFormat);

		// resultFormat = new PileupResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		// resultFormats.put(resultFormat.getC(), resultFormat);
		
		resultFormat = new BED6ResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		// resultFormat = new DebugResultFormat(parameters.getBaseConfig());
		// resultFormats.put(resultFormat.getC(), resultFormat);

		resultFormat = new VCF_ResultFormat(parameters.getBaseConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		return resultFormats;
	}

	@Override
	public void initCoordinateProvider() throws Exception {
		String[] pathnames1 = parameters.getSample1().getPathnames();
		String[] pathnames2 = parameters.getSample2().getPathnames();
		List<SAMSequenceRecord> records = getSAMSequenceRecords(pathnames1, pathnames2);
		coordinateProvider = new SAMCoordinateProvider(records);
	}

	@Override
	public AbstractParameters getParameters() {
		return parameters;
	}

	@Override
	public boolean parseArgs(String[] args) throws Exception {
		if (args == null || args.length != 2) {
			throw new ParseException("BAM File is not provided!");
		}

		SAMPathnameArg pa = new SAMPathnameArg(1, parameters.getSample1());
		pa.processArg(args[0]);
		pa = new SAMPathnameArg(2, parameters.getSample2());
		pa.processArg(args[1]);

		return true;
	}

	@Override
	public void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.setWidth(160);

		Set<AbstractACOption> acOptions = getACOptions();
		Options options = new Options();
		for (AbstractACOption acoption : acOptions) {
			options.addOption(acoption.getOption());
		}
		
		formatter.printHelp(JACUSA.NAME + " [OPTIONS] BAM1_1[,BAM1_2,BAM1_3,...] BAM2_1[,BAM2_2,BAM2_3,...]", options);
	}
	
}