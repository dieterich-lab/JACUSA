package accusa2.io.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

public class TmpOutputWriter implements Output {

	private String filename;
	private Writer writer;
	
	public TmpOutputWriter(String filename) throws IOException {
		this.filename = filename;
		
		writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename)));
	}

	public TmpOutputWriter(File file) throws IOException {
		this.filename = file.getAbsolutePath();
		writer = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename)));
	}

	@Override
	public String getName() {
		return "Default";
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
