package accusa2.io.output;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

public class TmpOutputReader {

	private BufferedReader br;
	
	public TmpOutputReader(String filename) throws IOException {
		br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(filename))));
	}

	public String readLine() throws IOException {
		return 	br.readLine();
	}

	public void close() throws IOException {
		if(br != null) {
			br.close();
			br = null;
		}
	}

}
