package geometry;

import com.vividsolutions.jts.geom.Geometry;

public abstract class Label {
	private boolean marked;
	/**
	 * @return Rough outline of label
	 */
	public abstract Geometry getGeometry();
	/**
	 * Saves label to raw data
	 */
	public abstract void render();
	
	public void setMarked(boolean marked) {
		this.marked = marked;
	}
	public boolean isMarked() {
		return marked;
	}
	public abstract void addTextBack();
}
