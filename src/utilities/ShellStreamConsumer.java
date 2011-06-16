package utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Takes output of content and prints it. Used to prevent lock when invoking external programs.
 */
public class ShellStreamConsumer extends Thread {
	public enum Type {STDERR, STDOUT}

	private InputStream stream;
	private Type type;
	
	public ShellStreamConsumer(InputStream stream, Type type){
		this.stream = stream;
		this.type = type;
	}
	
	public void run(){
		InputStreamReader reader = new InputStreamReader(this.stream);
		BufferedReader bufferedReader = new BufferedReader(reader);
		String line = null;
		do {
			try {
				line = bufferedReader.readLine();
				if(line!=null){
					if(type==Type.STDERR){
						System.err.println(line);
					} else if(type==Type.STDOUT){
						System.out.println(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}while(line!=null);
	}
}
