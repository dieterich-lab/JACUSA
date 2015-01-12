package jacusa.method.call;

import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.BaseConfigOption;
import jacusa.cli.options.BedCoordinatesOption;
import jacusa.cli.options.DebugOption;
import jacusa.cli.options.FilterConfigOption;
import jacusa.cli.options.FormatOption;
import jacusa.cli.options.HelpOption;
import jacusa.cli.options.MaxDepthOption;
import jacusa.cli.options.MaxThreadOption;
import jacusa.cli.options.MinBASQOption;
import jacusa.cli.options.MinCoverageOption;
import jacusa.cli.options.MinMAPQOption;
import jacusa.cli.options.PathnameArg;
import jacusa.cli.options.ResultFileOption;
import jacusa.cli.options.StatisticCalculatorOption;
import jacusa.cli.options.StatisticFilterOption;
import jacusa.cli.options.VersionOption;
import jacusa.cli.options.WindowSizeOption;
import jacusa.cli.options.pileupbuilder.TwoSamplePileupBuilderOption;
import jacusa.cli.options.sample.MaxDepthSampleOption;
import jacusa.cli.options.sample.MinBASQSampleOption;
import jacusa.cli.options.sample.MinCoverageSampleOption;
import jacusa.cli.options.sample.MinMAPQSampleOption;
//import jacusa.cli.options.sample.filter.FilterFlagOption;
//import jacusa.cli.options.sample.filter.FilterNHsamTagOption;
//import jacusa.cli.options.sample.filter.FilterNMsamTagOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.filter.factory.AbstractFilterFactory;
//import jacusa.filter.factory.BASQBiasFilterFactory;
import jacusa.filter.factory.DistanceFilterFactory;
import jacusa.filter.factory.HomopolymerFilterFactory;
import jacusa.filter.factory.HomozygousFilterFactory;
//import jacusa.filter.factory.MAPQBiasFilterFactory;
import jacusa.filter.factory.ReadPositionalBiasFilterFactory;
import jacusa.filter.factory.RareEventFilterFactory;
import jacusa.io.format.result.AbstractResultFormat;
import jacusa.io.format.result.BEDResultFormat;
//import jacusa.io.format.result.DebugResultFormat;
import jacusa.io.format.result.DefaultResultFormat;
import jacusa.io.format.result.PileupResultFormat;
//import jacusa.io.format.result.VCF_ResultFormat;
import jacusa.method.AbstractMethodFactory;
//import jacusa.method.call.statistic.ACCUSA2Statistic;
//import jacusa.method.call.statistic.DirichletBayesStatistic;
//import jacusa.method.call.statistic.DirichletStatistic;
//import jacusa.method.call.statistic.DirichletMOMsStatistic;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.method.call.statistic.dirmult.DirichletMultinomial;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialPooledError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialCompoundError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialEstimatedError;
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

	protected void initSampleACOptions(int sample, SampleParameters sampleParameters) {
		acOptions.add(new MinMAPQSampleOption(sample, sampleParameters));
		acOptions.add(new MinBASQSampleOption(sample, sampleParameters));
		acOptions.add(new MinCoverageSampleOption(sample, sampleParameters));
		acOptions.add(new MaxDepthSampleOption(sample, sampleParameters));
		/* TODO removed for inhouse release 
		acOptions.add(new FilterNHsamTagOption(sample, sampleParameters));
		acOptions.add(new FilterNMsamTagOption(sample, sampleParameters));
		*/
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
		acOptions.add(new MaxDepthOption(samples));
		// TODO removed for inhouse release
		// acOptions.add(new FilterFlagOption(samples));
		
		acOptions.add(new TwoSamplePileupBuilderOption(sample1, sample2));

		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		if (getResultFormats().size() == 1 ) {
			Character[] a = getResultFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getResultFormats().get(a[0]));
		} else {
			parameters.setFormat(getResultFormats().get(BEDResultFormat.CHAR));
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

		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new FilterConfigOption(parameters, getFilterFactories()));
		
		acOptions.add(new StatisticFilterOption(parameters.getStatisticParameters()));

		// 
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
	}

	@Override
	public TwoSampleCallWorkerDispatcher getInstance(CoordinateProvider coordinateProvider) throws IOException {
		if(instance == null) {
			instance = new TwoSampleCallWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}
	
	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = null;

		// TODO removed for inhouse release
		//statistic = new ACCUSA2Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);
		
		// TODO removed for inhouse release
		//statistic = new LR_SPEC_Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);
	
		// TODO removed for inhouse release
		//statistic = new LR_SENS_Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		// TODO removed for inhouse release
		//statistic = new DirichletBayesStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);
		
		// TODO removed for inhouse release
		//statistic = new DirichletMOMsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		/*
		statistic = new WeightedMethodOfMomentsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		*/

		// TODO removed for inhouse release
		//statistic = new DirichletStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		//statistic = new DirichletBayesLRStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		//statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMultinomial(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMultinomialPooledError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMultinomialCompoundError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMultinomialEstimatedError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		
		return statistics;
	}

	public Map<Character, AbstractFilterFactory<?>> getFilterFactories() {
		Map<Character, AbstractFilterFactory<?>> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory<?>>();

		AbstractFilterFactory<?>[] filterFactories = new AbstractFilterFactory[] {
				new ReadPositionalBiasFilterFactory(parameters),
//				new BASQBiasFilterFactory(parameters),
//				new MAPQBiasFilterFactory(parameters),
				new DistanceFilterFactory(parameters),
				new HomozygousFilterFactory(parameters),
				new HomopolymerFilterFactory(parameters),
				new RareEventFilterFactory(parameters),
//				new FDRFilterFactory(parameters.getStatisticParameters()),
		};
		for (AbstractFilterFactory<?> filterFactory : filterFactories) {
			abstractPileupFilters.put(filterFactory.getC(), filterFactory);
		}

		return abstractPileupFilters;
	}

	public Map<Character, AbstractResultFormat> getResultFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new DefaultResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		resultFormat = new PileupResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);
		
		resultFormat = new BEDResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		// TODO removed for inhouse release
		//resultFormat = new DebugResultFormat(parameters.getBaseConfig());
		//resultFormats.put(resultFormat.getC(), resultFormat);

		// TODO removed for inhouse release
		//resultFormat = new VCF_ResultFormat();
		//resultFormats.put(resultFormat.getC(), resultFormat);

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

		PathnameArg pa = new PathnameArg(1, parameters.getSample1());
		pa.processArg(args[0]);
		pa = new PathnameArg(2, parameters.getSample2());
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