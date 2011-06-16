package geometry;

import org.apache.batik.dom.svg.SVGOMTextElement;
import org.w3c.dom.svg.SVGCircleElement;

/**
 * A point like feature
 */
public class LabeledPointGene extends LabelGene {
	/**
	 * DOM element representing feature
	 */
	private SVGCircleElement circle;
	/**
	 * Horizontal feature position
	 */
	private double centerX;
	/**
	 * Vertical feature position
	 */
	private double centerY;
	/**
	 * Reference to DOM text element
	 */
	private SVGOMTextElement text;
	private static LineBreaker breaker = new LineBreaker(4);

	/**
	 * Constructs gene from DOM element
	 * 
	 * @param graphic
	 *            DOM element representing feature
	 * @param label
	 *            The features label
	 */
	public LabeledPointGene(SVGCircleElement graphic, SVGOMTextElement label) {
		circle = graphic;
		text = label;
		parsePosition();
		breaker.breakLine(text);
		initializeTextPositions();
		setPreferredPosition();
	}

	public LabeledPointGene(LabeledPointGene sample) {
		this.centerX = sample.centerX;
		this.centerY = sample.centerY;
		this.circle = sample.circle;
		this.text = sample.text;
		super.cloneAttributes(sample);
	}

	/**
	 * Calculates candidate positions
	 */
	private void initializeTextPositions() {
		float fontSize = text.getSVGContext().getFontSize();
		double horizontalShift = 0.5*fontSize;
		double verticalShift = 0.25*fontSize;
		TextParameter topRight = new TextParameter(text, centerX, centerY,
				centerX + horizontalShift, centerY - verticalShift, "start");
		addCandidate(topRight);

		TextParameter topLeft = new TextParameter(text, centerX, centerY,
				centerX - horizontalShift, centerY - verticalShift, "end");
		addCandidate(topLeft);

		TextParameter bottomRight = new TextParameter(text, centerX, centerY,
				centerX + horizontalShift, centerY + verticalShift, "start");
		addCandidate(bottomRight);

		TextParameter bottomLeft = new TextParameter(text, centerX, centerY,
				centerX - horizontalShift, centerY + verticalShift, "end");
		addCandidate(bottomLeft);
	}

	/**
	 * Sets label to top-right candidate position
	 */
	private void setPreferredPosition() {
		mutate();
	}

	/**
	 * Reads feature center from raw data
	 */
	private void parsePosition() {
		centerX = Double.parseDouble(circle.getAttribute("cx"));
		centerY = Double.parseDouble(circle.getAttribute("cy"));
	}

	/**
	 * Removes label from raw data
	 */
	public void discard() {
		text.getParentNode().removeChild(text);
		//circle.setAttribute("style", "fill:red");
		circle.getParentNode().removeChild(circle);
	}

	@Override
	public LabelGene clone() {
		return new LabeledPointGene(this);
	}
}
