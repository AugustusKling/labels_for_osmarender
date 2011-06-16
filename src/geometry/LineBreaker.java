package geometry;

import java.util.Arrays;

import org.apache.batik.dom.svg.SVGOMTextElement;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGTSpanElement;
import org.w3c.dom.svg.SVGTextElement;

/**
 * Introduces line breaks into labels so that their bounding box more or less matches a certain ratio
 */
public class LineBreaker {

	private final double desiredRatio;

	public LineBreaker(double desiredRatio) {
		this.desiredRatio = desiredRatio;
	}

	public void breakLine(SVGOMTextElement text) {
		String textContent = text.getTextContent();
		String[] contentParts = textContent.split("\\s+");
		if (contentParts.length > 1) {
			performBreaking(text, contentParts);
		}
	}

	private void performBreaking(SVGOMTextElement text, String[] contentParts) {
		Document doc = text.getOwnerDocument();
		int possibilities = (int) Math.pow(2, contentParts.length - 1);
		double bestRatioDifference = Float.MAX_VALUE;
		SVGTSpanElement[] bestLineBreak = null;
		for (int iter = 0; iter < possibilities; iter++) {
			String breaks = Integer.toBinaryString(iter);
			if(breaks.length()<contentParts.length-1){
				int requiredZerosCount = contentParts.length-1-breaks.length();
				char[] zeros = new char[requiredZerosCount];
				Arrays.fill(zeros, '0');
				breaks = new String(zeros)+breaks;
			}
			int breaksSoFar = 0;
			SVGTSpanElement[] lines = new SVGTSpanElement[Integer
					.bitCount(iter) + 1];
			StringBuffer lineContent = new StringBuffer();
			lineContent.append(contentParts[0]);
			for (int position = 0; position < breaks.length(); position++) {
				boolean shallBreak = breaks.charAt(position) == '1';
				if (shallBreak) {
					SVGTSpanElement line = (SVGTSpanElement) doc
							.createElementNS(text.getNamespaceURI(), "tspan");
					line
							.appendChild(doc.createTextNode(lineContent
									.toString()));
					lines[breaksSoFar] = line;
					breaksSoFar++;
					lineContent.setLength(0);
				} else {
					lineContent.append(" ");
				}
				lineContent.append(contentParts[position+1]);
			}
			SVGTSpanElement line = (SVGTSpanElement) doc.createElementNS(text
					.getNamespaceURI(), "tspan");
			line.appendChild(doc.createTextNode(lineContent.toString()));
			lines[breaksSoFar] = line;
			replaceContent(text, lines);
			float ratio = text.getBBox().getWidth()
					/ text.getBBox().getHeight();
			double ratioDifference = Math.abs(ratio - desiredRatio);
			if (ratioDifference < bestRatioDifference) {
				bestRatioDifference = ratioDifference;
				bestLineBreak = lines;
			}
		}
		replaceContent(text, bestLineBreak);
	}

	private void replaceContent(SVGOMTextElement text, SVGTSpanElement[] lines) {
		float fontSize = text.getSVGContext().getFontSize();
		removeAllChildren(text);
		for (SVGTSpanElement line : lines) {
			line.setAttribute("x", text.getAttribute("x"));
			if(text.hasChildNodes()){
				line.setAttribute("dy", fontSize+"px");
			}
			text.appendChild(line);
		}
	}

	private void removeAllChildren(SVGTextElement text) {
		while (text.getLastChild() != null) {
			text.removeChild(text.getLastChild());
		}
	}

}
