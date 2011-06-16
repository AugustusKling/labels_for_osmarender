package runables;

import geometry.Map;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.batik.dom.svg.SVGOMCircleElement;
import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.w3c.dom.NodeList;

import utilities.ShellStreamConsumer;
import utilities.ShellStreamConsumer.Type;

/**
 * Invokes map processing
 */
public class ProcessMap {
	/** Reference to map data */
	private File mapData;
	/** Reference to map style */
	private File styleFile;

	/**
	 * Renders map using Osmarender and improves label positions afterwards
	 * @throws Exception
	 */
	public void createMap() throws Exception {
		File osmarenderedFile = invokeOsmarenderXsltProc();

		Map map = new Map();
		map.load("file:" + osmarenderedFile.getAbsolutePath());
		osmarenderedFile.delete();

		NodeList pointNodes = map
				.getNodes("//svg:g[@id='map']/svg:circle[@osm:id=../svg:text/@osm:id]");
		System.out.println("nodes: "+pointNodes.getLength());
		for (int eIter = 0; eIter < pointNodes.getLength(); eIter++) {
			SVGOMCircleElement graphic = (SVGOMCircleElement) pointNodes
					.item(eIter);
			map.addPointObject(graphic);
		}

		NodeList areaNodes = map
				.getNodes("//svg:g[@id='map']/svg:path[@osm:id=../svg:text/@osm:id]");
		for (int eIter = 0; eIter < areaNodes.getLength(); eIter++) {
			SVGOMPathElement graphic = null;
			graphic = (SVGOMPathElement) areaNodes.item(eIter);
			map.addAreaObject(graphic);
		}

		NodeList lineNodes = map
				.getNodes("//svg:g[@id='map']/svg:text[contains(@class, 'highway-track-caption') or contains(@class, 'highway-name') or contains(@class, 'highway-ref')]");
		for (int eIter = 0; eIter < lineNodes.getLength(); eIter++) {
			SVGOMTextElement graphic = (SVGOMTextElement) lineNodes.item(eIter);
			map.addLineObject(graphic);
		}

		// Mutate any number of times
		Date start = new Date();
		double bestTotalOverlap = Double.MAX_VALUE;
		long timeOfLastImprovementOrDiscard = System.currentTimeMillis();
		while (true) {
			map.mutate();
			long runtime = new Date().getTime() - start.getTime();
			double totalOverlap = map.getTotalOverlaps();
			if (totalOverlap < bestTotalOverlap) {
				bestTotalOverlap = totalOverlap;
				timeOfLastImprovementOrDiscard = System
						.currentTimeMillis();
			}
			// Abort optimization if no problems persist or after 20 minutes
			if (totalOverlap == 0 || runtime > 20 * 60000) {
				System.out.println("Finished after " + (runtime / 60)
						+ "s");
				System.out.println("Worked on "
						+ (pointNodes.getLength()
								+ lineNodes.getLength() + areaNodes
								.getLength()) + " features.");
				break;
			}
			// If no improvement was made for 1 second, remove a map feature to simplify the problem 
			if (System.currentTimeMillis()
					- timeOfLastImprovementOrDiscard > 1000) {
				// Kick out a feature label if too much overlaps persist after some
				// mutations
				map.discard(1);
				timeOfLastImprovementOrDiscard = System
						.currentTimeMillis();
			}
		}

		map.fixObjects();
		map.save(mapData.getAbsolutePath() + "-processed.svg");
	}

	/**
	 * Runs Osmarender using xsltproc
	 * @return Osmarender's output (the unoptimized map)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private File invokeOsmarenderXsltProc() throws IOException, InterruptedException {
		final String osmarender = "lib/osmarender.xsl";
		File osmarenderOutput = File.createTempFile("map", ".svg");
		
		String[] command = new String[5];
		command[0] = "xsltproc";
		command[1] = "-o";
		command[2] = osmarenderOutput.getCanonicalPath();
		command[3] = osmarender;
		command[4] = styleFile.getCanonicalPath();
		
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(command);
		// Consume streams to prevent locking
		new ShellStreamConsumer(process.getInputStream(), Type.STDOUT);
		new ShellStreamConsumer(process.getErrorStream(), Type.STDERR);
		
		int returnValue = process.waitFor();
		if(returnValue!=0){
			throw new RuntimeException("Map generation failed");
		}
		
		return osmarenderOutput;
	}

	public void setMapData(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException("Map data file does not exist.");
		}
		this.mapData = file;
	}

	public void setStyleFile(File file) {
		if (!file.exists()) {
			throw new IllegalArgumentException("Style file does not exist.");
		}
		this.styleFile = file;
	}

}
