package geometry;

import org.apache.batik.dom.svg.SVGOMPathElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.parser.PathParser;
import org.w3c.dom.svg.SVGElement;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

/**
 * An area with a label near its center
 */
public class LabeledAreaGene extends LabelGene {
	private SVGOMTextElement text;
	/**
	 * Reference to DOM element
	 */
	private SVGOMPathElement shape;
	private Polygon path;
	private boolean isEnoughSpaceForLabel=true;
	private static LineBreaker breaker= new LineBreaker(4);

	public LabeledAreaGene(SVGElement graphic, SVGOMTextElement svgText) {
		shape = (SVGOMPathElement) graphic;
		this.text = svgText;
		breaker.breakLine(text);
		parseShape();
		generateCandidatePositions();
	}

	private LabeledAreaGene(LabeledAreaGene sample) {
		this.text = sample.text;
		this.shape = sample.shape;
		this.path = sample.path;
		super.cloneAttributes(sample);
	}

	private void generateCandidatePositions() {
		Polygon p = new Polygon(new LinearRing(path.getExteriorRing().getCoordinateSequence(), path.getFactory()), new LinearRing[0], path.getFactory());
		Geometry inset = p.buffer(-3);
		if(inset.isEmpty()){
			inset = path;
		}
		Point interiorPoint = inset
				.getInteriorPoint();
		CenterAreaLabel interior = new CenterAreaLabel(text, interiorPoint);
		/*if(path.getArea()<(interior.getGeometry().getArea()/2)){
			// Don't add label if label area is at least double of shape area
			isEnoughSpaceForLabel = false;
			return;
		}*/
		addCandidate(interior);
		
		float labelHeight = text.getBBox().getHeight();
		
		Point topPoint = interiorPoint.getFactory().createPoint(new Coordinate(interiorPoint.getX(), interiorPoint.getY()-labelHeight/2));
		CenterAreaLabel top = new CenterAreaLabel(text, topPoint);
		addCandidate(top);

		Point lowerPoint = interiorPoint.getFactory().createPoint(new Coordinate(interiorPoint.getX(), interiorPoint.getY()+labelHeight/2));
		CenterAreaLabel lower = new CenterAreaLabel(text, lowerPoint);
		addCandidate(lower);

		CenterAreaLabel centeriod = new CenterAreaLabel(text, inset
				.getCentroid());
		addCandidate(centeriod);
	}
	
	@Override
	public boolean isFeasiblePositionAvailable(Geometry avoids) {
		return isEnoughSpaceForLabel && super.isFeasiblePositionAvailable(avoids);
	}

	/**
	 * Reads the shape of the raw data and creates a polygon with holes
	 */
	private void parseShape() {
		PathParser p = new PathParser();
		MultipolygonParser multipolygonHandler = new MultipolygonParser();
		p.setPathHandler(multipolygonHandler);
		p.parse(shape.getAttribute("d"));
		path = multipolygonHandler.getMultipolygon();
	}

	@Override
	public void discard() {
		text.getParentNode().removeChild(text);
		//shape.setAttribute("style", "fill:red;");
	}

	@Override
	public LabelGene clone() {
		return new LabeledAreaGene(this);
	}

}
