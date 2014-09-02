package accusa2.cli.parameters;

import accusa2.io.format.result.AbstractResultFormat;

public interface hasResultFormat {

	AbstractResultFormat getResultFormat();
	void setResultFormat(AbstractResultFormat resultFormat);

}