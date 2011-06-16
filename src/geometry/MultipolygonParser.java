package geometry;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PathHandler;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Parses a SVG path description and returns a polygon (with holes).
 * 
 * Does only work for SVG paths that are describing polygons – not for arcs.
 */
public class MultipolygonParser implements PathHandler {
	private LinkedList<Coordinate> coordinates = new LinkedList<Coordinate>();
	private boolean processingShell;
	private LinkedList<Coordinate> shellCoordinates;
	private LinkedList<LinkedList<Coordinate>> holes = new LinkedList<LinkedList<Coordinate>>();

	@Override
	public void startPath() throws ParseException {
		processingShell=true;
	}
	
	@Override
	public void movetoRel(float x, float y) throws ParseException {
		addRelativeMove(x, y);
	}
	
	private void addRelativeMove(float x, float y) {
		Coordinate lastCoordinates;
		try {
			lastCoordinates = coordinates.getLast();
		} catch (NoSuchElementException e) {
			lastCoordinates = new Coordinate(0, 0);
		}
		coordinates.add(new Coordinate(lastCoordinates.x+x, lastCoordinates.y+y));
	}

	@Override
	public void movetoAbs(float x, float y) throws ParseException {
		coordinates.add(new Coordinate(x, y));
	}
	
	@Override
	public void linetoVerticalRel(float y) throws ParseException {
	}
	
	@Override
	public void linetoVerticalAbs(float y) throws ParseException {
	}
	
	@Override
	public void linetoRel(float x, float y) throws ParseException {
		addRelativeMove(x, y);
	}
	
	@Override
	public void linetoHorizontalRel(float x) throws ParseException {
	}
	
	@Override
	public void linetoHorizontalAbs(float x) throws ParseException {
	}
	
	@Override
	public void linetoAbs(float x, float y) throws ParseException {
		coordinates.add(new Coordinate(x, y));
	}
	
	@Override
	public void endPath() throws ParseException {
	}
	
	@Override
	public void curvetoQuadraticSmoothRel(float x, float y)
			throws ParseException {
	}
	
	@Override
	public void curvetoQuadraticSmoothAbs(float x, float y)
			throws ParseException {
	}
	
	@Override
	public void curvetoQuadraticRel(float x1, float y1, float x, float y)
			throws ParseException {
	}
	
	@Override
	public void curvetoQuadraticAbs(float x1, float y1, float x, float y)
			throws ParseException {
	}
	
	@Override
	public void curvetoCubicSmoothRel(float x2, float y2, float x, float y)
			throws ParseException {
	}
	
	@Override
	public void curvetoCubicSmoothAbs(float x2, float y2, float x, float y)
			throws ParseException {
	}
	
	@Override
	public void curvetoCubicRel(float x1, float y1, float x2, float y2,
			float x, float y) throws ParseException {
	}
	
	@Override
	public void curvetoCubicAbs(float x1, float y1, float x2, float y2,
			float x, float y) throws ParseException {
	}
	
	@Override
	public void closePath() throws ParseException {
		if(!coordinates.getFirst().equals(coordinates.getLast())){
			coordinates.add((Coordinate) coordinates.getFirst().clone());
		}
		if(processingShell){
			shellCoordinates = coordinates;
			processingShell=false;
		} else {
			holes.add(coordinates);
		}
		coordinates = new LinkedList<Coordinate>();
	}
	
	@Override
	public void arcRel(float rx, float ry, float xAxisRotation,
			boolean largeArcFlag, boolean sweepFlag, float x, float y)
			throws ParseException {
	}
	
	@Override
	public void arcAbs(float rx, float ry, float xAxisRotation,
			boolean largeArcFlag, boolean sweepFlag, float x, float y)
			throws ParseException {
	}

	/**
	 * @return Polygon with holes
	 */
	public Polygon getMultipolygon() {
		final GeometryFactory gf = Helper.getGeometryFactory();
		CoordinateSequence shellSequence = gf.getCoordinateSequenceFactory().create(shellCoordinates.toArray(new Coordinate[0]));
		LinearRing shell = new LinearRing(shellSequence, gf);
		LinearRing[] holes = new LinearRing[this.holes.size()];
		LinkedList<LinearRing> rings = new LinkedList<LinearRing>();
		rings.add(shell);
		for(int holeIter=0; holeIter<this.holes.size(); holeIter++){
			LinkedList<Coordinate> holeCoordinates = this.holes.get(holeIter);
			CoordinateSequence holeSequence = gf.getCoordinateSequenceFactory().create(holeCoordinates.toArray(new Coordinate[0]));
			holes[holeIter] = new LinearRing(holeSequence, gf);
			rings.add(new LinearRing(holeSequence, gf));
		}
		Collections.sort(rings, new Comparator<LinearRing>() {
			@Override
			public int compare(LinearRing r1, LinearRing r2) {
				double o1 = new Polygon(r1, new LinearRing[0], gf).getArea();
				double o2 = new Polygon(r2, new LinearRing[0], gf).getArea();
				double difference = o2-o1;
				if(difference<0){
					return -1;
				}
				if(difference>0){
					return 1;
				}
				return 0;
			}
		});
		LinearRing exteriorRing = rings.remove(0);
		LinearRing[] interiorRings = rings.toArray(new LinearRing[rings.size()]);
		Polygon path = new Polygon(exteriorRing, interiorRings, gf);
		return path;
	}
}
