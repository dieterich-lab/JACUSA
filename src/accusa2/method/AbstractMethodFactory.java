package accusa2.method;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import accusa2.cli.options.AbstractACOption;
import accusa2.process.parallelpileup.dispatcher.AbstractWorkerDispatcher;
import accusa2.process.parallelpileup.worker.AbstractWorker;
import accusa2.util.CoordinateProvider;

public abstract class AbstractMethodFactory {

	protected Set<AbstractACOption> acOptions;

	private String name;
	private String desc;

	public abstract void initACOptions();
	public abstract AbstractWorkerDispatcher<? extends AbstractWorker> getInstance(CoordinateProvider coordinateProvider) throws IOException; 

	public AbstractMethodFactory(String name, String desc) {
		this.name = name;
		this.desc = desc;

		acOptions = new HashSet<AbstractACOption>();
	}

	public Set<AbstractACOption> getACOptions() {
		return acOptions;
	}

	public final String getName() {
		return name;
	}

	public final String getDescription() {
		return desc;
	}

}