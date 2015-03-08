package jacusa.method.call;

import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.BaseConfigOption;
import jacusa.cli.options.BedCoordinatesOption;
import jacusa.cli.options.DebugOption;
import jacusa.cli.options.FilterConfigOption;
import jacusa.cli.options.FormatOption;
import jacusa.cli.options.HelpOption;
import jacusa.cli.options.MaxThreadOption;
import jacusa.cli.options.SAMPathnameArg;
import jacusa.cli.options.ResultFileOption;
import jacusa.cli.options.StatisticCalculatorOption;
import jacusa.cli.options.StatisticFilterOption;
import jacusa.cli.options.VersionOption;
import jacusa.cli.options.WindowSizeOption;
//import jacusa.cli.options.filter.FilterNHsamTagOption;
//import jacusa.cli.options.filter.FilterNMsamTagOption;
import jacusa.cli.options.pileupbuilder.OneSamplePileupBuilderOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.OneSampleCallParameters;
import jacusa.cli.parameters.SampleParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.filter.factory.DistanceFilterFactory;
import jacusa.filter.factory.HomopolymerFilterFactory;
import jacusa.filter.factory.HomozygousFilterFactory;
import jacusa.filter.factory.MaxAlleleCountFilterFactors;
import jacusa.filter.factory.RareEventFilterFactory;
import jacusa.io.format.result.AbstractResultFormat;
import jacusa.io.format.result.DefaultResultFormat;
import jacusa.io.format.result.PileupResultFormat;
import jacusa.method.AbstractMethodFactory;
import jacusa.method.call.statistic.DirichletMOMsStatistic;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.method.call.statistic.lr.LR_SENS_Statistic;
import jacusa.method.call.statistic.lr.LR_SPEC_Statistic;
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
	}

	public void initACOptions() {
		SampleParameters sampleA = parameters.getSample1();
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
		
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
		
		/*
		acOptions.add(new FilterNHsamTagOption(parameters.getSample1()));
		acOptions.add(new FilterNMsamTagOption(parameters.getSample1()));
		*/
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

		statistic = new LR_SPEC_Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new LR_SENS_Statistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		statistic = new DirichletMOMsStatistic(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);

		return statistics;
	}

	public Map<Character, AbstractFilterFactory<?>> getFilterFactories() {
		Map<Character, AbstractFilterFactory<?>> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory<?>>();

		AbstractFilterFactory<?>[] filters = new AbstractFilterFactory[] {
				new DistanceFilterFactory(parameters),
				new HomozygousFilterFactory(),
				new MaxAlleleCountFilterFactors(),
				new HomopolymerFilterFactory(parameters),
				new RareEventFilterFactory(parameters),
		};
		for (AbstractFilterFactory<?> filter : filters) {
			abstractPileupFilters.put(filter.getC(), filter);
		}

		return abstractPileupFilters;
	}

	public Map<Character, AbstractResultFormat> getFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new DefaultResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		resultFormat = new PileupResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
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
	public AbstractParameters getParameters() {
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
		
		formatter.printHelp(JACUSA.NAME + " [OPTIONS] BAM1_1[,BAM1_2,BAM1_3,...]", options);
	}
	
}
