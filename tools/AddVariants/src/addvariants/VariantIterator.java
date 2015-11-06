package addvariants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class VariantIterator implements Iterator<Variant> {

	private File file;
	private BufferedReader br;
	
	private String line;

	public VariantIterator(String bedPathname) {
		file = new File(bedPathname);
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		if(line != null) {
			return true;
		}

		try {
			line = br.readLine();
			while((line != null && line.length() == 0)) {
				line = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return line != null;
	}

	@Override
	public Variant next() {
		if(!hasNext()) {
			return null;
		}
		Variant coord = Variant.parseLine(line);
		line = null;
		return coord;
	}

	public Variant getNext() {
		if(!hasNext()) {
			return null;
		}
		Variant coord = Variant.parseLine(line);
		return coord;
	}

	@Override
	public void remove() {
		// not needed
	}
	
	public void close() {
		if(br != null ){
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			br = null;
		}
	}

}
