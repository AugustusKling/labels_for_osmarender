package geometry;

import org.w3c.dom.svg.SVGRect;

public class Rectangle implements SVGRect {
	float height;
	float width;
	float x;
	float y;

	public Rectangle(SVGRect bBox) {
		this.height = bBox.getHeight();
		this.width = bBox.getWidth();
		this.x = bBox.getX();
		this.y = bBox.getY();
	}

	@Override
	public float getHeight() {
		return height;
	}

	/**
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}

}
