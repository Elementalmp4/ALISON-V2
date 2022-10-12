package main.java.de.voidtech.alison.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ResourceLoader {

	public List<String> getResource(String filename) {
		List<String> lines = new ArrayList<>();
		try {
			InputStream dataInStream = getClass().getClassLoader().getResourceAsStream(filename);
            assert dataInStream != null;
            BufferedReader br = new BufferedReader(new InputStreamReader(dataInStream));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}
}