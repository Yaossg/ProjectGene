package project_gene.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import static net.minecraftforge.common.ForgeConfigSpec.DoubleValue;

public class PGConfig {
	
	public static final ForgeConfigSpec CONFIG_SPEC;
	public static final PGConfig CONFIG;
	static {
		Pair<PGConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(PGConfig::new);
		CONFIG = specPair.getLeft();
		CONFIG_SPEC = specPair.getRight();
	}
	
	
	public final DoubleValue baseMutationRate;


	public PGConfig(ForgeConfigSpec.Builder builder) {
		baseMutationRate = builder
				.comment("Base rate of gene mutation of each animal parent ProjectGene introduced")
				.translation("project_gene.config.baseMutationRate")
				.defineInRange("baseMutationRate", 0.0042, 0, 1);
	}
	
}
