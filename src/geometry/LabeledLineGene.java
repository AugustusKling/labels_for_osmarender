package geometry;

import org.apache.batik.dom.svg.SVGOMTextElement;

/**
 * A line feature with a single label
 */
public class LabeledLineGene extends LabelGene {
	
	private final SVGOMTextElement use;

	public LabeledLineGene(SVGOMTextElement graphic){
		this.use = graphic;
		generateLabelPositions();
	}

	private LabeledLineGene(LabeledLineGene sample) {
		this.use = sample.use;
		super.cloneAttributes(sample);
	}

	/**
	 * Calculates feasible label positions
	 */
	private void generateLabelPositions() {
		boolean addedCandidate = false;
		// Start off with label that is in the middle of the line
		LineTextParameter candidate = new LineTextParameter(use, 50);
		try {
			candidate.getGeometry();
			addCandidate(candidate);
			addedCandidate=true;
		} catch (FalloffException e){
			System.err.println("Invalid offset "+50);
		} catch (TightBendException e){
			System.err.println("Bend too much");
		}
		// Add other positions along line
		for(int percentage=10; percentage<100; percentage=percentage+10){
			if(percentage==50){
				continue;
			}
			candidate = new LineTextParameter(use, percentage);
			try {
				candidate.getGeometry();
				addCandidate(candidate);
				addedCandidate=true;
			} catch (FalloffException e){
				System.err.println("Invalid offset "+percentage);
				break;
			} catch (TightBendException e){
				System.err.println("Bend too much");
			}
		}
		if(addedCandidate==false){
			System.err.println("No valid candidate for: "+use.getTextContent());
			// Clear label if it does not fit anywhere
			use.setTextContent("");
		}
	}

	@Override
	public
	void discard() {
		use.getParentNode().removeChild(use);
	}

	@Override
	public LabelGene clone() {
		return new LabeledLineGene(this);
	}

}
