package jacusa.method.window;


import jacusa.JACUSA;
import jacusa.cli.options.AbstractACOption;
import jacusa.cli.options.BaseConfigOption;
import jacusa.cli.options.BedCoordinatesOption;
import jacusa.cli.options.DebugOption;
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
// import jacusa.cli.options.pileupbuilder.TwoSamplePileupBuilderOption;
import jacusa.cli.options.sample.MaxDepthSampleOption;
import jacusa.cli.options.sample.MinBASQSampleOption;
import jacusa.cli.options.sample.MinCoverageSampleOption;
import jacusa.cli.options.sample.MinMAPQSampleOption;
import jacusa.cli.options.sample.filter.FilterFlagOption;
import jacusa.cli.options.sample.filter.FilterNHsamTagOption;
import jacusa.cli.options.sample.filter.FilterNMsamTagOption;
import jacusa.cli.parameters.AbstractParameters;
import jacusa.cli.parameters.CLI;
import jacusa.cli.parameters.SampleParameters;
import jacusa.cli.parameters.TwoSampleCallParameters;
import jacusa.io.format.result.AbstractResultFormat;
import jacusa.io.format.result.BED6ResultFormat;
import jacusa.io.format.result.BEDWindowResultFormat;
import jacusa.method.AbstractMethodFactory;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.method.call.statistic.dirmult.DirichletMultinomial;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialPooledError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialCompoundError;
import jacusa.method.call.statistic.dirmult.DirichletMultinomialEstimatedError;
import jacusa.pileup.dispatcher.call.TwoSampleWindowCallWorkerDispatcher;
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

public class TwoSampleWindowCallFactory extends AbstractMethodFactory {

	private TwoSampleCallParameters parameters;

	private static TwoSampleWindowCallWorkerDispatcher instance;

	public TwoSampleWindowCallFactory() {
		super("window-2", "Call variants in windows - two samples");
		parameters = new TwoSampleCallParameters();
	}

	protected void initSampleACOptions(int sample, SampleParameters sampleParameters) {
		acOptions.add(new MinMAPQSampleOption(sample, sampleParameters));
		acOptions.add(new MinBASQSampleOption(sample, sampleParameters));
		acOptions.add(new MinCoverageSampleOption(sample, sampleParameters));
		acOptions.add(new MaxDepthSampleOption(sample, sampleParameters));
		acOptions.add(new FilterNHsamTagOption(sample, sampleParameters));
		acOptions.add(new FilterNMsamTagOption(sample, sampleParameters));
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
		acOptions.add(new FilterFlagOption(samples));
		
		// NO strand support in initial version acOptions.add(new TwoSamplePileupBuilderOption(sample1, sample2));

		acOptions.add(new BedCoordinatesOption(parameters));
		acOptions.add(new ResultFileOption(parameters));
		if (getResultFormats().size() == 1 ) {
			Character[] a = getResultFormats().keySet().toArray(new Character[1]);
			parameters.setFormat(getResultFormats().get(a[0]));
		} else {
			parameters.setFormat(getResultFormats().get(BED6ResultFormat.CHAR));
			acOptions.add(new FormatOption<AbstractResultFormat>(parameters, getResultFormats()));
		}

		acOptions.add(new MaxThreadOption(parameters));

		if (getStatistics().size() == 1 ) {
			String[] a = getStatistics().keySet().toArray(new String[1]);
			parameters.getStatisticParameters().setStatisticCalculator(getStatistics().get(a[0]));
		} else {
			acOptions.add(new StatisticCalculatorOption(parameters.getStatisticParameters(), getStatistics()));
		}

		acOptions.add(new BaseConfigOption(parameters));
		acOptions.add(new StatisticFilterOption(parameters.getStatisticParameters()));

		// 
		acOptions.add(new DebugOption(parameters));
		acOptions.add(new HelpOption(CLI.getSingleton()));
		acOptions.add(new VersionOption(CLI.getSingleton()));
	}

	@Override
	public TwoSampleWindowCallWorkerDispatcher getInstance(CoordinateProvider coordinateProvider) throws IOException {
		if(instance == null) {
			instance = new TwoSampleWindowCallWorkerDispatcher(coordinateProvider, parameters);
		}
		return instance;
	}
	
	public Map<String, StatisticCalculator> getStatistics() {
		Map<String, StatisticCalculator> statistics = new TreeMap<String, StatisticCalculator>();

		StatisticCalculator statistic = null;

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

	public Map<Character, AbstractResultFormat> getResultFormats() {
		Map<Character, AbstractResultFormat> resultFormats = new HashMap<Character, AbstractResultFormat>();

		AbstractResultFormat resultFormat = new BEDWindowResultFormat(parameters.getBaseConfig(), parameters.getFilterConfig());
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

		if (parameters.getBedPathname().length() == 0) {
			throw new Exception("BED file needed for window calling!");
		}

		PathnameArg pa = new PathnameArg(1, parameters.getSample1());
		pa.processArg(args[0]);
		if (parameters.getSample1().getPathnames().length > 1) {
			throw new Exception("Replicates for window calling not supported!");
		}
		
		pa = new PathnameArg(2, parameters.getSample2());
		pa.processArg(args[1]);
		if (parameters.getSample2().getPathnames().length > 1) {
			throw new Exception("Replicates for window calling not supported!");
		}

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
		
		formatter.printHelp(JACUSA.NAME + " [OPTIONS] BAM1 BAM2", options);
	}
	
}