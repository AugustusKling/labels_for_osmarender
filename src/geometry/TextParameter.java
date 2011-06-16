package geometry;

import org.apache.batik.dom.svg.SVGOMTextElement;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGRect;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

public class TextParameter extends Label {

	private Geometry geometry;
	private final SVGOMTextElement text;
	private final double x;
	private final double y;
	private final String textAnchor;
	private final double centerX;
	private final double centerY;

	public TextParameter(SVGOMTextElement text, double centerX, double centerY,
			double x, double y, String textAnchor) {
		this.text = text;
		this.centerX = centerX;
		this.centerY = centerY;
		this.x = x;
		this.y = y;
		this.textAnchor = textAnchor;
	}

	public Geometry getGeometry() {
		if (geometry != null) {
			return geometry;
		}
		render();
		GeometryFactory gf = Helper.getGeometryFactory();
		Coordinate[] labelCorners = new Coordinate[5];
		SVGRect textBox = text.getBBox();
		labelCorners[0] = new Coordinate(textBox.getX(), textBox.getY());
		labelCorners[1] = new Coordinate(textBox.getX() + textBox.getWidth(),
				textBox.getY());
		labelCorners[2] = new Coordinate(textBox.getX() + textBox.getWidth(),
				textBox.getY() + textBox.getHeight());
		labelCorners[3] = new Coordinate(textBox.getX(), textBox.getY()
				+ textBox.getHeight());
		labelCorners[labelCorners.length-1] = labelCorners[0];
		Geometry geo = gf
				.createPolygon(gf.createLinearRing(labelCorners), null);
		Geometry[] geometries = new Geometry[2];
		geometries[0] =geo;
		geometries[1] = gf.createPoint(new Coordinate(centerX, centerY));
		GeometryCollection gc = new GeometryCollection(geometries, gf);
		geometry = gc.convexHull();
		geometry = geometry.buffer(text.getSVGContext().getFontSize()/2);
		return geometry;
	}

	public void render() {
		text.setAttribute("x", Double.toString(x));
		text.setAttribute("y", Double.toString(y));
		NodeList textParts = text.getElementsByTagName("tspan");
		for (int pIter = 0; pIter < textParts.getLength(); pIter++) {
			Element textPart = (Element) textParts.item(pIter);
			textPart.setAttribute("x", Double.toString(x));
		}
		if (isMarked()) {
			text.setAttribute("style", "text-anchor:" + textAnchor);
			for (int pIter = 0; pIter < textParts.getLength(); pIter++) {
				Element textPart = (Element) textParts.item(pIter);
				textPart.setAttribute("style", "text-anchor:" + textAnchor);
			}
		} else {
			text.setAttribute("style", "text-anchor:" + textAnchor + ";");
			for (int pIter = 0; pIter < textParts.getLength(); pIter++) {
				Element textPart = (Element) textParts.item(pIter);
				textPart.setAttribute("style", "text-anchor:" + textAnchor);
			}
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
		textBack.setAttribute("style", "fill:red;opacity:0.5;");
		text.getParentNode().insertBefore(textBack, text);
	}

}
