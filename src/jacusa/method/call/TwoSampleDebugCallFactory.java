package jacusa.method.call;

import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.FilterConfigOption;
import jacusa.cli.options.FormatOption;
import jacusa.cli.options.HelpOption;
import jacusa.cli.options.PathnameArg;
import jacusa.cli.options.ResultFileOption;
import jacusa.cli.options.StatisticCalculatorOption;
import jacusa.cli.options.StatisticFilterOption;
import jacusa.cli.options.sample.MaxDepthSampleOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.filter.factory.AbstractFilterFactory;
import jacusa.filter.factory.OutlierFilterFactory;
import jacusa.filter.factory.ZeroCountFilterFactory;
import jacusa.io.format.result.AbstractResultFormat;
import jacusa.io.format.result.BED6ResultFormat;
import jacusa.io.format.result.DebugResultFormat;
import jacusa.method.AbstractMethodFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.method.call.statistic.dirmult.DirichletMultinomial;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialPooledError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialCompoundError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialEstimatedError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialRobustCompoundError;
import jacusa.pileup.dispatcher.call.TwoSampleDebugCallWorkerDispatcher;
import jacusa.util.coordinateprovider.BedCoordinateProvider;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class TwoSampleDebugCallFactory extends AbstractMethodFactory {

	private TwoSampleCallParameters parameters;

	private static TwoSampleDebugCallWorkerDispatcher instance;

	public TwoSampleDebugCallFactory() {
		super("debug", "Debug statistics");
		parameters = new TwoSampleCallParameters();
	}

	protected void initSampleACOptions(int sample, SampleParameters sampleParameters) {
		acOptions.add(new MaxDepthSampleOption(sample, sampleParameters));
	}
	
	public void initACOptions() {
		// sample specific setting
		SampleParameters sample1 = parameters.getSample1();
		SampleParameters sample2 = parameters.getSample2();

		for (int sampleI = 1; sampleI <= 2; ++sampleI) {
			initSampleACOptions(sampleI, sample1);
			initSampleACOptions(sampleI, sample2);
		}
		// global settings

		acOptions.add(new ResultFileOption(parameters));
		if (getResultFormats().size() == 1 ) {
			Character[] a = getResultFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getResultFormats().get(a[0]));
		} else {
			parameters.setFormat(getResultFormats().get(BED6ResultFormat.CHAR));
			acOptions.add(new FormatOption<AbstractResultFormat>(parameters, getResultFormats()));
		}

		if (getStatistics().size() == 1 ) {
			String[] a = getStatistics().keySet().toArray(new String[1]);
			parameters.getStatisticParameters().setStatisticCalculator(getStatistics().get(a[0]));
		} else {
			acOptions.add(new StatisticCalculatorOption(parameters.getStatisticParameters(), getStatistics()));
		}

		acOptions.add(new StatisticFilterOption(parameters.getStatisticParameters()));
		acOptions.add(new FilterConfigOption(parameters, getFilterFactories()));

		// 
		acOptions.add(new HelpOption(CLI.getSingleton()));
	}

	@Override
	public TwoSampleDebugCallWorkerDispatcher getInstance(CoordinateProvider coordinateProvider) throws IOException {
		if(instance == null) {
			instance = new TwoSampleDebugCallWorkerDispatcher(coordinateProvider, parameters);
		}

		return instance;
	}

	public Map<Character, AbstractFilterFactory<?>> getFilterFactories() {
		Map<Character, AbstractFilterFactory<?>> abstractPileupFilters = new HashMap<Character, AbstractFilterFactory<?>>();

		AbstractFilterFactory<?>[] filterFactories = new AbstractFilterFactory[] {
				new ZeroCountFilterFactory(parameters.getStatisticParameters()),
				new OutlierFilterFactory(parameters.getStatisticParameters())
		};
		for (AbstractFilterFactory<?> filterFactory : filterFactories) {
			abstractPileupFilters.put(filterFactory.getC(), filterFactory);
		}

		return abstractPileupFilters;
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

		statistic = new DirichletMultinomialRobustCompoundError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		
		statistic = new DirichletMultinomialEstimatedError(parameters.getBaseConfig(), parameters.getStatisticParameters());
		statistics.put(statistic.getName(), statistic);
		
		return statistics;
	}

	public Map<Character, AbstractResultFormat> getResultFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new DebugResultFormat(parameters.getBaseConfig());
		resultFormats.put(resultFormat.getC(), resultFormat);

		return resultFormats;
	}

	@Override
	public void initCoordinateProvider() throws Exception {
		String[] pathnames1 = parameters.getSample1().getPathnames();
		coordinateProvider = new BedCoordinateProvider(pathnames1[0]);
	}

	@Override
	public AbstractParameters getParameters() {
		return parameters;
	}

	@Override
	public boolean parseArgs(String[] args) throws Exception {
		if (args == null || args.length != 1) {
			throw new ParseException("Input File is not provided!");
		}

		PathnameArg pa = new PathnameArg(parameters.getSample1());
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

		formatter.printHelp(JACUSA.NAME + " [OPTIONS] input.txt", options);
	}
	
}