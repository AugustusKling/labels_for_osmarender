package utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Logger {

	private static long start;
	private static HashMap<File, FileWriter> writers= new HashMap<File, FileWriter>();

	public static void initialize() {
		start = System.currentTimeMillis();
	}

	public static void log(String message) {
		System.out.println(System.currentTimeMillis()-start+" "+message);
	}

	public static void log(File csvFile, String string) throws IOException {
		FileWriter writer = writers.get(csvFile);
		if(writer==null){
			writer = new FileWriter(csvFile);
			writers.put(csvFile, writer);
		}
		writer.append(string);
		writer.append('\n');
		writer.flush();
	}

}
