package geometry;

import java.util.LinkedList;
import java.util.Random;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

public abstract class LabelGene {
	/**
	 * List of candidate positions
	 */
	private LinkedList<Label> candidates = new LinkedList<Label>();
	/**
	 * Identifier of candidate position currently in use
	 */
	private int usedCandidate;
	/**
	 * Bounding box around all candidate positions
	 */
	private Geometry maxExtend;
	/**
	 * Generator for random positions
	 */
	private static Random r = new Random();

	/**
	 * Checks if there is at lease one candidate position that is not
	 * overlapping the forbidden area
	 * 
	 * @param avoids
	 *            Forbidden area
	 * @return True if non-overlapping position exists
	 */
	public boolean isFeasiblePositionAvailable(Geometry avoids) {
		for (Label candidate : candidates) {
			if (!candidate.getGeometry().intersects(avoids)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return Bounding box of candidate position in use
	 */
	public Geometry getGeometry(){
		return getUsedCandidate().getGeometry();
	}

	/**
	 * Highlights candidate position
	 * 
	 * @category Debugging
	 */
	public void setMarked(boolean state) {
		getUsedCandidate().setMarked(state);
	}

	/**
	 * Chooses a random candidate position
	 */
	public void mutate() {
		usedCandidate = r.nextInt(candidates.size());
	}
	
	protected void addCandidate(Label candidate){
		candidates.add(candidate);
	}
	
	protected Label getUsedCandidate(){
		return candidates.get(usedCandidate);
	}

	public abstract void discard();
	
	/**
	 * Sets the current position to raw data
	 */
	public void render() {
		getUsedCandidate().render();
		//getUsedCandidate().addTextBack();
	}
	
	/**
	 * @return Bounding box containing all candidate positions
	 */
	public Geometry getMaxExtend(){
		if(maxExtend!=null){
			return maxExtend.getEnvelope();
		}
		maxExtend = new MultiPolygon(null, Helper.getGeometryFactory());
		for (Label candidate : candidates) {
			maxExtend = maxExtend.union(candidate.getGeometry());
		}
		return maxExtend.getEnvelope();
	}

	@Override
	public abstract LabelGene clone();

	protected final void cloneAttributes(LabelGene sample) {
		this.candidates = sample.candidates;
		this.maxExtend = sample.maxExtend;
		this.usedCandidate = sample.usedCandidate;
	}
	
}
