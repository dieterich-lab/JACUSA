package jacusa.method.call;

import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.BaseConfigOption;
import jacusa.cli.options.BedCoordinatesOption;
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
import jacusa.cli.options.ShowReferenceOption;
import jacusa.cli.options.StatisticCalculatorOption;
import jacusa.cli.options.StatisticFilterOption;
import jacusa.cli.options.ThreadWindowSizeOption;
import jacusa.cli.options.VersionOption;
import jacusa.cli.options.WindowSizeOption;
import jacusa.cli.options.pileupbuilder.OneSamplePileupBuilderOption;
import jacusa.cli.options.sample.InvertStrandOption;
import jacusa.cli.options.sample.filter.FilterFlagOption;
import jacusa.cli.options.sample.filter.FilterNHsamTagOption;
import jacusa.cli.options.sample.filter.FilterNMsamTagOption;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.OneSampleCallParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.filter.factory.DistanceFilterFactory;
import jacusa.filter.factory.HomopolymerFilterFactory;
import jacusa.filter.factory.INDEL_DistanceFilterFactory;
import jacusa.filter.factory.MaxAlleleCountFilterFactors;
import jacusa.filter.factory.MinDifferenceFilterFactory;
import jacusa.filter.factory.RareEventFilterFactory;
import jacusa.filter.factory.ReadPositionDistanceFilterFactory;
import jacusa.filter.factory.SpliceSiteDistanceFilterFactory;
import jacusa.io.format.AbstractOutputFormat;
import jacusa.io.format.BED6OneSampleResultFormat;
//import jacusa.io.format.BED6ResultFormat;
import jacusa.io.format.VCF_ResultFormat;
import jacusa.method.AbstractMethodFactory;
import jacusa.method.call.statistic.ACCUSA2Statistic;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialCompoundError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialRobustCompoundError;
import jacusa.pileup.dispatcher.call.OneSampleCallWorkerDispatcher;
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

public class OneSampleCallFactory extends AbstractMethodFactory {

	private OneSampleCallParameters parameters = new OneSampleCallParameters();
	private static OneSampleCallWorkerDispatcher instance;
	
	public OneSampleCallFactory() {
		super("call-1", "Call variants - one sample");
		parameters = new OneSampleCallParameters();
	}
	
	public void initACOptions() {
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters[] samples = new SampleParameters[] {
				sample1
		};
		
		acOptions.add(new MinMAPQOption(samples));
		acOptions.add(new MinBASQOption(samples));
		acOptions.add(new MinCoverageOption(samples));
		acOptions.add(new MaxDepthOption(parameters));
		acOptions.add(new FilterFlagOption(samples));
		final int sampleI = 1;
		acOptions.add(new FilterNHsamTagOption(sampleI, sample1));
		acOptions.add(new FilterNMsamTagOption(sampleI, sample1));
		acOptions.add(new InvertStrandOption(sampleI, sample1));
		
		
		acOptions.add(new OneSamplePileupBuilderOption(sample1));
		
		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		if(getFormats().size() == 1 ) {
			Character[] a = getFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getFormats().get(a[0]));
		} else {
			parameters.setFormat(new BED6OneSampleResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig(), parameters.showReferenceBase()));
			acOptions.add(new FormatOption<AbstractOutputFormat>(parameters, getFormats()));
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

		acOptions.add(new FilterConfigOption(parameters, getFilterFactories()));
		acOptions.add(new FilterModusOption(parameters));
		
		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new StatisticFilterOption(parameters.getStatisticParameters()));
		
		// acOptions.add(new DebugOption(parameters));
		acOptions.add(new ShowReferenceOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
	}

	@Override
	public OneSampleCallWorkerDispatcher getInstance(String[] pathnames1, String[] pathnames2, CoordinateProvider coordinateProvider) throws IOException {
		if(instance == null) {
			instance = new OneSampleCallWorkerDispatcher(pathnames1, pathnames2, coordinateProvider, parameters);
		}
		return instance;
	}
	
	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = null;

		statistic = new ACCUSA2Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		
		statistic = new DirichletMultinomialCompoundError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMultinomialRobustCompoundError	(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		return statistics;
	}

	public Map<Character, AbstractFilterFactory<?>> getFilterFactories() {
		Map<Character, AbstractFilterFactory<?>> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory<?>>();

		AbstractFilterFactory<?>[] filters = new AbstractFilterFactory[] {
				new DistanceFilterFactory(parameters),
				new INDEL_DistanceFilterFactory(parameters),
				new ReadPositionDistanceFilterFactory(parameters),
				new SpliceSiteDistanceFilterFactory(parameters),
				new MaxAlleleCountFilterFactors(parameters),
				new HomopolymerFilterFactory(parameters),
				new RareEventFilterFactory(parameters),
				new MinDifferenceFilterFactory(parameters),
		};
		for (AbstractFilterFactory<?> filter : filters) {
			abstractPileupFilters.put(filter.getC(), filter);
		}

		return abstractPileupFilters;
	}

	public Map<Character, AbstractOutputFormat> getFormats() {
		Map<Character, AbstractOutputFormat> resultFormats = new HashMap<Character, AbstractOutputFormat>();

		AbstractOutputFormat resultFormat = null;
		
		resultFormat = new BED6OneSampleResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig(), parameters.showReferenceBase());
		resultFormats.put(resultFormat.getC(), resultFormat);

		/*
		resultFormat = new BED6ResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig(), parameters.showReferenceBase());
		resultFormats.put(resultFormat.getC(), resultFormat);
		*/
		
		resultFormat = new VCF_ResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		return resultFormats;
	}

	@Override
	public void initCoordinateProvider() throws Exception {
		String[] pathnames = parameters.getSample1().getPathnames();

		List<SAMSequenceRecord> records = getSAMSequenceRecords(pathnames);
		coordinateProvider = new SAMCoordinateProvider(records);
	}

	@Override
	public OneSampleCallParameters getParameters() {
		return parameters;
	}
	
	@Override
	public boolean parseArgs(String[] args) throws Exception {
		if (args == null || args.length != 1) {
			throw new ParseException("BAM File is not provided!");
		}
		
		SAMPathnameArg pa = new SAMPathnameArg(1, parameters.getSample1());
		pa.processArg(args[0]);
		
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
		
		formatter.printHelp(JACUSA.JAR + " [OPTIONS] BAM1_1[,BAM1_2,BAM1_3,...]", options);
	}
	
}
