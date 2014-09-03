package accusa2.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

public class TmpWriter implements Output {

	private String filename;
	private Writer writer;

	public TmpWriter(String filename) throws IOException {
		this.filename = filename;

		writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename)));
	}

	@Override
	public String getName() {
		return "tmp";
	}

	@Override
	public String getInfo() {
		return filename;
	}

	@Override
	public void write(String line) throws IOException {
		writer.write(line + "\n");
	}

	@Override
	public void close() throws IOException {
		if(writer != null) {
			writer.close();
			writer = null;
		}
	}

}