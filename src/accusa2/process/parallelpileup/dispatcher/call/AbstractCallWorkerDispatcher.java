package accusa2.process.parallelpileup.dispatcher.call;

import java.io.IOException;

import accusa2.cli.parameters.StatisticParameters;
import accusa2.io.Output;
import accusa2.io.OutputWriter;
import accusa2.io.format.result.AbstractResultFormat;
import accusa2.method.call.statistic.StatisticCalculator;
import accusa2.process.parallelpileup.dispatcher.AbstractWorkerDispatcher;
import accusa2.process.parallelpileup.worker.AbstractCallWorker;
import accusa2.util.CoordinateProvider;

public abstract class AbstractCallWorkerDispatcher<T extends AbstractCallWorker> extends AbstractWorkerDispatcher<T> {

	private final StatisticParameters statisticParameters;
	private final boolean isDebug; 
	private final Output output;
	private final Output filtered;
	private final AbstractResultFormat format;
	
	public AbstractCallWorkerDispatcher(
			final CoordinateProvider coordinateProvider, 
			final int maxThreads,
			final StatisticParameters statisticParameters,
			final Output output, 
			final AbstractResultFormat format,
			final boolean isDebug) throws IOException {
		super(coordinateProvider, maxThreads, output, format, isDebug);
		this.statisticParameters = statisticParameters;
		this.isDebug 	= isDebug;
		this.output 	= output;
		filtered		= new OutputWriter(output.getInfo() + ".filtered");
		
		this.format 	= format;
	}

	protected abstract String getHeader();

	@Override
	protected void processTmpLine(final String line) throws IOException {
		final double p = getFormat().extractValue(line);
		StatisticCalculator statisticCalculator = statisticParameters.getStatisticCalculator();  
		
		if (isDebug) {
			if (! statisticCalculator.filter(p)) {
				getOutput().write(line + "\t" + p);
			}
		} else {
			if (p < 0.0) {
				getFiltered().write(line + "\t" + p);
			} else if (! statisticCalculator.filter(p)) {
				getOutput().write(line + "\t" + p);
			}
		}
	}
	
	protected Output getOutput() {
		return output;
	}

	protected Output getFiltered() {
		return filtered;
	}

	protected AbstractResultFormat getFormat() {
		return format;
	}
	
}
