package accusa2.process.parallelpileup.dispatcher;



import java.io.File;

import java.io.IOException;
import java.util.List;

import accusa2.cli.Parameters;
import accusa2.io.format.AbstractResultFormat;
import accusa2.io.format.TmpResultFormat;
import accusa2.io.output.Output;
import accusa2.io.output.TmpOutputReader;
import accusa2.io.output.TmpOutputWriter;
import accusa2.process.parallelpileup.worker.ACCUSA25_ParallelPileupWorker;
import accusa2.util.AnnotatedCoordinate;

public class ACCUSA25_ParallelPileupWorkerDispatcher extends AbstractParallelPileupWorkerDispatcher<ACCUSA25_ParallelPileupWorker> {

	public ACCUSA25_ParallelPileupWorkerDispatcher(List<AnnotatedCoordinate> coordinates, Parameters parameters) {
		super(coordinates, parameters);
	}

	@Override
	protected void processFinishedWorker(ACCUSA25_ParallelPileupWorker parallelPileupWorker) {
		synchronized (comparisons) {
			comparisons += parallelPileupWorker.getComparisons();
		}

		synchronized (tmpOutputs) {
			for(int i : parallelPileupWorker.getTmpOutputWriters().keySet()) {
				tmpOutputs[i] = parallelPileupWorker.getTmpOutputWriters().get(i);
			}
		}
	}

	@Override
	protected ACCUSA25_ParallelPileupWorker buildNextParallelPileupWorker() {
		return new ACCUSA25_ParallelPileupWorker(this, next(), parameters);
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
		
		for(final TmpOutputWriter tmpOutputWriter : tmpOutputs) {
			try {
				tmpOutputWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			try {
				final TmpOutputReader tmpOutputReader = new TmpOutputReader(tmpOutputWriter.getInfo());

				String line = null;
				while((line = tmpOutputReader.readLine()) != null) {
					final double p = resultFormat.extractValue(line);
					if(p <= parameters.getFDR()) {
						output.write(line + "\t" + p);
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
		
		// FIXME add FDR for p-values
		/*
		if(parameters.getDebug()){
			statisticContainer.write("stat.txt");
		}
		*/
	}
	
}
