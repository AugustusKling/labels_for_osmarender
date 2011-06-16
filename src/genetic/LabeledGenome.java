package genetic;

import geometry.Helper;
import geometry.LabelGene;
import geometry.LabelGeneOverlap;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Holds positions for several labeled features
 * 
 * @param <T> Type of gene in genome
 */
public class LabeledGenome<T extends LabelGene> implements Iterable<LabelGene> {
	/**
	 * List of all genes
	 */
	private Vector<LabelGene> genes = new Vector<LabelGene>();
	
	/**
	 * Areas where no labels should end up. Such a space for the map key.
	 */
	private final Geometry avoids;
	
	/**
	 * Total area of overlapping labels.
	 */
	private Double cachedTotalOverlap;

	/**
	 * @param avoids Area that should remain label free.
	 */
	public LabeledGenome(Geometry avoids) {
		this.avoids = avoids;
	}

	/**
	 * Copies another genome to create a clone
	 * @param sample Base genome
	 */
	public LabeledGenome(LabeledGenome<T> sample) {
		this.genes = new Vector<LabelGene>();
		for(LabelGene gene: sample.genes){
			this.genes.add(gene.clone());
		}
		this.avoids = sample.avoids;
	}

	/**
	 * @return Total area with label overlaps
	 */
	public double getTotalOverlaps() {
		if(cachedTotalOverlap != null){
			return cachedTotalOverlap;
		}
		GeometryFactory factory = Helper.getGeometryFactory();
		double totalOverlap = 0;
		for (LabelGene gene : this) {
			double overlapArea = getGeneOverlap(gene, factory);
			if (overlapArea > 0) {
				totalOverlap = totalOverlap + overlapArea;
				gene.setMarked(true);
			} else {
				gene.setMarked(false);
			}
		}
		cachedTotalOverlap = totalOverlap;
		return totalOverlap;
	}
	
	/**
	 * Calculates the area that a gene's label shares with other labels
	 * 
	 * @param gene
	 *            The gene responsible for the label in question
	 * @param factory
	 * @return Area of overlap
	 */
	private double getGeneOverlap(LabelGene gene, GeometryFactory factory) {
		Geometry geometry = gene.getGeometry();
		LinkedList<Geometry> overlappingObjects = new LinkedList<Geometry>();
		overlappingObjects.add(avoids.intersection(geometry));
		for (LabelGene otherGene : this) {
			if (otherGene != gene) {
				if (gene.getMaxExtend().intersects(otherGene.getMaxExtend())) {
					overlappingObjects.add(otherGene.getGeometry()
							.intersection(geometry));
				}
			}
		}
		GeometryCollection overlap = new GeometryCollection(overlappingObjects.toArray(new Geometry[0]), factory);
		double overlapArea = overlap.getArea();
		return overlapArea;
	}
	
	/**
	 * Mutates all genes representing overlapping labels to try out new label positions
	 */
	public void mutate() {
		cachedTotalOverlap = null;
		GeometryFactory factory = Helper.getGeometryFactory();
		for (LabelGene gene : this) {
			double overlapArea = getGeneOverlap(gene, factory);
			if (overlapArea > 0) {
				gene.mutate();
			}
		}
	}
	
	/**
	 * Removes some labels from the map
	 * 
	 * @param amount
	 *            Number of labels to remove
	 */
	public void discard(int amount) {
		System.out.println("Discarding");
		GeometryFactory factory = Helper.getGeometryFactory();
		LinkedList<LabelGeneOverlap> overlappingGenes = new LinkedList<LabelGeneOverlap>();
		for (int geneI = this.size() - 1; geneI > 0; geneI--) {
			LabelGene gene = this.get(geneI);
			double overlapArea = getGeneOverlap(gene, factory);
			if (overlapArea > 0) {
				overlappingGenes.add(new LabelGeneOverlap(gene, overlapArea));
			}
		}
		Collections.sort(overlappingGenes);
		Geometry clearedArea = null;
		for(int geneI=0; geneI<amount; geneI++){
			LabelGene gene = overlappingGenes.get(geneI).getGene();
			if(clearedArea == null){
				clearedArea = gene.getGeometry();
			} else {
				clearedArea = clearedArea.union(gene.getGeometry());
			}
			gene.discard();
			genes.remove(gene);
		}
		for(LabelGene gene:this){
			Geometry geneGeometry = gene.getGeometry();
			if(geneGeometry.intersects(clearedArea)){
				gene.mutate();
			}
		}
	}
	
	@Override
	public synchronized LabeledGenome<T> clone() {
		return new LabeledGenome<T>(this);
	}

	/**
	 * Merges 2 genomes to create a child that inherit from the parents
	 * @param other Genome of the other parent
	 * @return Child with characteristics of this genome and other parent
	 */
	public LabeledGenome<LabelGene> merge(LabeledGenome<LabelGene> other) {
		if(size()!=other.size()){
			throw new IllegalArgumentException("Sizes of genomes mismatch");
		}
		int splitPoint = new Random().nextInt(this.size());
		double splitGeneLocation = this.get(splitPoint).getGeometry().getCentroid().getX();
		LabeledGenome<LabelGene> child = new LabeledGenome<LabelGene>(avoids);
		for(int geneIter=0; geneIter<size(); geneIter++){
			double geneLocation = this.get(geneIter).getGeometry().getCentroid().getX();
			if(geneLocation<splitGeneLocation){
				child.add(this.get(geneIter));
			} else {
				child.add(other.get(geneIter));
			}
		}
		return child;
	}

	/**
	 * @return The number of genes in the genome
	 */
	public int size() {
		return genes.size();
	}

	public LabelGene get(int geneIter) {
		return genes.get(geneIter);
	}

	public void set(int geneIter, LabelGene clone) {
		genes.set(geneIter, clone);
	}

	public void add(T gene) {
		genes.add(gene);
	}

	@Override
	public Iterator<LabelGene> iterator() {
		return genes.iterator();
	}

}
