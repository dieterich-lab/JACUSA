package accusa2.process.parallelpileup.dispatcher;

import java.io.File;
import java.io.IOException;

import accusa2.cli.Parameters;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.format.TmpResultFormat;
import accusa2.io.output.Output;
import accusa2.io.output.TmpOutputReader;
import accusa2.io.output.TmpOutputWriter;
import accusa2.pileup.ParallelPileup;
import accusa2.process.parallelpileup.worker.ACCUSA2_ParallelPileupWorker;
import accusa2.util.CoordinateProvider;
import accusa2.util.DiscriminantStatisticContainer;
import accusa2.util.StatisticContainer;

public class ACCUSA2_ParallelPileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<ACCUSA2_ParallelPileupWorker> {

	private final StatisticContainer statisticContainer; 

	public ACCUSA2_ParallelPileupWorkerDispatcher(CoordinateProvider coordinateProvider, Parameters parameters) {
		super(coordinateProvider, parameters);

		statisticContainer = new DiscriminantStatisticContainer();
	}

	@Override
	protected void processFinishedWorker(ACCUSA2_ParallelPileupWorker parallelPileupWorker) {
		synchronized (comparisons) {
			comparisons += parallelPileupWorker.getComparisons();
		}

		synchronized (statisticContainer) {
			try {
				statisticContainer.addContainer(parallelPileupWorker.getStatisticContainer());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Deprecated
		/*
		synchronized (tmpOutputs) {
			for(int i : parallelPileupWorker.getTmpOutputWriters().keySet()) {
				tmpOutputs[i] = parallelPileupWorker.getTmpOutputWriters().get(i);
			}
		}
		*/
	}

	@Override
	protected ACCUSA2_ParallelPileupWorker buildNextParallelPileupWorker_helper() {
		return new ACCUSA2_ParallelPileupWorker(this, next(), parameters);
	}

	@Override
	protected void writeOuptut() {
		final Output output = parameters.getOutput();
		final AbstractResultFormat resultFormat = new TmpResultFormat(parameters.getResultFormat());

		// write Header
		try {
			output.write(resultFormat.getHeader());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for(final TmpOutputWriter tmpOutputWriter : tmpOutputWriters) {
			try {
				tmpOutputWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				final TmpOutputReader tmpOutputReader = new TmpOutputReader(tmpOutputWriter.getInfo());

				String line = null;
				while((line = tmpOutputReader.readLine()) != null) {
					final double value = resultFormat.extractValue(line);

					final ParallelPileup parallelPileup = resultFormat.extractParallelPileup(line);

					final double fdr = statisticContainer.getFDR(value, parallelPileup);

					/*
					if( fdr > 1  || fdr < 0 ) {
						//throw new Exception("FDR: " + fdr + " Value: " + value);
						System.err.println("FDR: " + fdr + " Value: " + value);
						System.err.println(line);
						System.err.println("he_he_R: " + discriminantStatisticContainer.getHe_he_R().getCumulativeCount(value));
						System.err.println("he_he_V: " + discriminantStatisticContainer.getHe_he_V().getCumulativeCount(value));
						System.err.println("ho_he_R: " + discriminantStatisticContainer.getHo_he_R().getCumulativeCount(value));
						System.err.println("ho_he_V: " + discriminantStatisticContainer.getHo_he_V().getCumulativeCount(value));
					}
					*/

					if(fdr <= parameters.getFDR()) {
						output.write(line + resultFormat.getSEP() + fdr);
					}
				}
				tmpOutputReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(!parameters.getDebug()){
				new File(tmpOutputWriter.getInfo()).delete();
			}
		}
		/* FIXME 
		if(parameters.getDebug()){
			statisticContainer.write("stat.txt");
		}
		*/
	}
	
}
