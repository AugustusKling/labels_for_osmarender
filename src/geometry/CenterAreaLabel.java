package geometry;

import org.apache.batik.dom.svg.SVGOMTextElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGRect;
import org.w3c.dom.svg.SVGTextElement;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

/**
 * Label in the center of an area
 */
public class CenterAreaLabel extends Label {

	private final SVGTextElement text;
	private Geometry geometry;
	private final Point position;
	public CenterAreaLabel(SVGOMTextElement text,
			Point position) {
		this.text = text;
		SVGRect textBox = text.getBBox();
		float height = textBox.getHeight();
		double x = position.getX();
		double y = position.getY()-height/2;
		this.position = Helper.getGeometryFactory().createPoint(new Coordinate(x, y));
	}

	@Override
	public Geometry getGeometry() {
		if (geometry != null) {
			return geometry;
		}
		render();
		geometry = getGeometryForBoundingBox(text.getBBox());
		return geometry;
	}

	private Geometry getGeometryForBoundingBox(SVGRect bBox) {
		GeometryFactory gf = Helper.getGeometryFactory();
		Coordinate[] labelCorners = new Coordinate[5];
		SVGRect textBox = text.getBBox();
		float width = textBox.getWidth();
		float height = textBox.getHeight();
		float x = textBox.getX();
		float y = textBox.getY();
		labelCorners[0] = new Coordinate(x, y);
		labelCorners[1] = new Coordinate(x + width,
				y);
		labelCorners[2] = new Coordinate(x + width,
				y + height);
		labelCorners[3] = new Coordinate(x, y
				+ height);
		labelCorners[4] = labelCorners[0];
		return gf.createPolygon(gf.createLinearRing(labelCorners), null);
	}

	@Override
	public void render() {
		String textX = Double.toString(position.getX());
		text.setAttribute("x", textX);
		text.setAttribute("y", Double.toString(position.getY()));
		NodeList lines = text.getElementsByTagName("tspan");
		for (int i=0; i<lines.getLength(); i++) {
			Element line = (Element) lines.item(i);
			line.setAttribute("x", textX);
		}
		if (isMarked()) {
			//shape.setAttribute("style", "fill:red;");
		} else {
			text.setAttribute("style", "");
		}
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
		textBack.setAttribute("style", "fill:green;opacity:0.5;");
		text.getParentNode().insertBefore(textBack, text);
	}

}
