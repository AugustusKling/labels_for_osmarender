package geometry;

public class LabelGeneOverlap implements Comparable<LabelGeneOverlap> {

	private final LabelGene gene;
	private final double overlapArea;

	public LabelGeneOverlap(LabelGene gene, double overlapArea) {
		this.gene = gene;
		this.overlapArea = overlapArea;
	}

	public LabelGene getGene() {
		return gene;
	}

	@Override
	public int compareTo(LabelGeneOverlap o) {
		double difference = o.overlapArea-overlapArea;
		if(difference==0){
			return 0;
		}
		if(difference<0){
			return -1;
		}
		return 1;
	}

}
