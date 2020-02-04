package project_gene.gene;

import java.util.Random;

public enum Genotype {
	dominant_homozygote(true, true), // AA
	dominant_heterozygote(true, false), // Aa
	recessive(false, true); //aa
	
	public final boolean dominant;
	public final boolean homozygote;


	Genotype(boolean dominant, boolean homozygote) {
		this.dominant = dominant;
		this.homozygote = homozygote;
	}

	private static final Random segregator = new Random();
	public boolean seg() {
		return homozygote ? dominant : segregator.nextBoolean();
	}

	private static final Random mutor = new Random();
	public boolean seg_mut(double rate) {
		boolean seg = seg();
		boolean mut = mutor.nextDouble() < rate;
		if (mut) seg = !seg;
		return seg;
	}
	
	public static Genotype combine(boolean P1, boolean P2) {
		return P1 && P2 ? dominant_homozygote : P1 || P2 ? dominant_heterozygote : recessive;
	}
	
	public String nameOf(String geneType) {
		String dominant = nameOf(geneType, true), recessive = nameOf(geneType, false);
		switch (this) {
			case dominant_homozygote:
				return dominant + dominant;
			case dominant_heterozygote:
				return dominant + recessive;
			case recessive:
				return recessive + recessive;
			default:
				throw new AssertionError();
		}
	}

	public static String nameOf(String geneType, boolean dominant) {
		return geneType.length() == 1
				? dominant ? geneType.toUpperCase() : geneType.toLowerCase()
				: geneType + (dominant ? "⁺" : "⁻");
	}

	private static final Random randomizer = new Random();
	public static Genotype randomize() {
		return values()[randomizer.nextInt(3)];
	}
	
	public int dominance() {
		return 3 - ordinal();
	}
}
