package geometry;

import java.util.LinkedList;

import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.dom.svg.SVGOMTextPathElement;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGRect;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class LineTextParameter extends Label {

	private final SVGOMTextElement text;
	private Geometry geometry;
	private SVGOMTextPathElement textPath;
	private final int percentage;

	public LineTextParameter(SVGOMTextElement text, int percentage) {
		this.text = text;
		this.percentage = percentage;
		this.textPath = (SVGOMTextPathElement) text.getElementsByTagName("textPath").item(0);
	}

	@Override
	public Geometry getGeometry() {
		if(geometry!=null){
			return geometry;
		}
		render();
		GeometryFactory gf = Helper.getGeometryFactory();
		Geometry[] geometries = new Geometry[textPath.getNumberOfChars()];
		LinkedList<Point> characterCentroids = new LinkedList<Point>();
		for(int charnum=0; charnum<textPath.getNumberOfChars(); charnum++){
			try {
				SVGRect textBox = textPath.getExtentOfChar(charnum);
				Coordinate[] labelCorners = new Coordinate[5];
				labelCorners[0] = new Coordinate(textBox.getX(), textBox.getY());
				labelCorners[1] = new Coordinate(textBox.getX() + textBox.getWidth(),
						textBox.getY());
				labelCorners[2] = new Coordinate(textBox.getX() + textBox.getWidth(),
						textBox.getY() + textBox.getHeight());
				labelCorners[3] = new Coordinate(textBox.getX(), textBox.getY()
						+ textBox.getHeight());
				labelCorners[4] = labelCorners[0];
				geometries[charnum]=gf.createPolygon(gf.createLinearRing(labelCorners), null);
				characterCentroids.add(geometries[charnum].getCentroid());
			} catch (DOMException e){
				throw new FalloffException("Label longer than space available along line", e);
			}
		}
		// Fail for labels in tight bends
		if(characterCentroids.size()>2){
			Point firstPoint = characterCentroids.getFirst();
			Point secondPoint = characterCentroids.get(1);
			double lastAngle = (180*Math.atan((secondPoint.getY()-firstPoint.getY())/(secondPoint.getX()-firstPoint.getX())))/Math.PI;
			for(int pointIter=1; pointIter<characterCentroids.size()-1; pointIter++){
				firstPoint = characterCentroids.get(pointIter);
				secondPoint = characterCentroids.get(pointIter+1);
				double currentAngle = (180*Math.atan((secondPoint.getY()-firstPoint.getY())/(secondPoint.getX()-firstPoint.getX())))/Math.PI;
				if(Math.abs(lastAngle-currentAngle)>80){
					throw new TightBendException("Label occurred in tight bend.");
				}
			}
		}
		GeometryCollection gc = gf.createGeometryCollection(geometries);
		geometry = gc.convexHull();
		return geometry;
	}

	@Override
	public void render() {
		textPath = (SVGOMTextPathElement) text.getElementsByTagName("textPath").item(0);
		if(isMarked()){
			textPath.setAttribute("style", "fill:red");
		} else {
			textPath.setAttribute("style", "");
		}
		textPath.setAttribute("startOffset", percentage+"%");
		Node pathClone = textPath.cloneNode(true);
		text.removeChild(textPath);
		text.appendChild(pathClone);
		this.textPath = (SVGOMTextPathElement) pathClone;
	}

	@Override
	public void addTextBack() {
		Element textBack = text.getOwnerDocument().createElement("path");
		String d = "M";
		for (Coordinate c : geometry.getCoordinates()) {
			if(d.length()>1){
				d=d+" L";
			}
			d = d + " " + c.x + "," + c.y;
		}
		textBack.setAttribute("d", d);
		textBack.setAttribute("style", "fill:yellow;opacity:0.5;");
		text.getParentNode().insertBefore(textBack, text);
	}

}
