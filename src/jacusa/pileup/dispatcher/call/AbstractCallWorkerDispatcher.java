package jacusa.pileup.dispatcher.call;

import jacusa.cli.parameters.StatisticParameters;
import jacusa.io.Output;
import jacusa.io.OutputWriter;
import jacusa.io.format.result.AbstractResultFormat;
import jacusa.method.call.statistic.StatisticCalculator;
import jacusa.pileup.dispatcher.AbstractWorkerDispatcher;
import jacusa.pileup.worker.AbstractCallWorker;
import jacusa.util.coordinateprovider.CoordinateProvider;

import java.io.IOException;

public abstract class AbstractCallWorkerDispatcher<T extends AbstractCallWorker> extends AbstractWorkerDispatcher<T> {

	private final StatisticParameters statisticParameters;
	private final boolean isDebug; 
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
		filtered		= new OutputWriter(output.getInfo() + ".filtered");
		
		this.format 	= format;
	}

	protected abstract String getHeader();

	@Override
	protected void processTmpLine(final String line) throws IOException {
		final double p = getFormat().extractValue(line);
		final String filterInfo = getFormat().getFilterInfo(line);
		StatisticCalculator statisticCalculator = statisticParameters.getStatisticCalculator();  

		if (isDebug) {
			if (! statisticCalculator.filter(p)) {
				getOutput().write(line);
			}
		} else {
			if (! filterInfo.equals(Character.toString(getFormat().getEMPTY()))) {
				getFiltered().write(line);
			} else if (! statisticCalculator.filter(p)) {
				getOutput().write(line);
			}
		}
	}

	@Override
	protected void writeOuptut() {
		super.writeOuptut();

		try {
			filtered.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected Output getFiltered() {
		return filtered;
	}

	protected AbstractResultFormat getFormat() {
		return format;
	}

}