package org.transitclock.quickstart.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractResource {

	public static void main(String[] args) throws IOException {
		extractResourceNIO(ExtractResource.class.getClassLoader(), "testnio.txt");
		File file = extractResourceFile(ExtractResource.class.getClassLoader(), "testfile.txt");
		BufferedReader br = new BufferedReader(new FileReader(file));
		for (String line; (line = br.readLine()) != null;) {
			System.out.println(line);
		}
		System.out.println(file.getAbsolutePath());
		br.close();
	}

	public static void extractResourceNIO(ClassLoader classLoader, String name) throws IOException {
		InputStream inputStream1 = ExtractResource.class.getClassLoader().getResourceAsStream(name);

		Path destination = Paths.get(name);

		if (!Files.exists(destination)) {
			Files.copy(inputStream1, destination);
		}
		inputStream1.close();
	}

	public static File extractResourceFile(ClassLoader classLoader, String name) throws IOException {
		InputStream in = null;
		FileOutputStream out = null;
		String namebits[]=name.split("\\.");
		File tempFile = File.createTempFile(namebits[0], "."+namebits[1]);
		try {
			in = classLoader.getResourceAsStream(name);

			out = new FileOutputStream(tempFile);

			int c;
			while ((c = in.read()) != -1) {
				out.write(c);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
		return tempFile;
	}
}
