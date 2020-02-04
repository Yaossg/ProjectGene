package project_gene.entity.core;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.extensions.IForgeEntity;
import project_gene.ProjectGene;
import project_gene.config.PGConfig;
import project_gene.gene.GenePool;

public interface IGenePooled<E extends IGenePooled> extends IForgeEntity {
	GenePool getGenePool();
	GenePool getDefaultGenePool();
	void setGenePool(GenePool genePool);
	
	@SuppressWarnings("unchecked")
	default E child(AgeableEntity other) {
		E child = (E) getEntity().getType().create(getEntity().world);
		if (child != null) {
			E e = (E) other;
			child.setGenePool(GenePool.seg_comb_mut(getGenePool(), e.getGenePool(), mutationRate(), e.mutationRate()));
		}
		return child;
	}
	
	default double mutationRate() {
		AgeableEntity ageable = (AgeableEntity) getEntity();
		int stable = getAmplifier(ageable.getActivePotionEffect(ProjectGene.stable));
		int mutable = getAmplifier(ageable.getActivePotionEffect(ProjectGene.mutable));
		return PGConfig.CONFIG.baseMutationRate.get() * Math.pow(1.5, mutable - stable);
	}
	
	static int getAmplifier(EffectInstance instance) {
		if (instance == null) return 0;
		return instance.getAmplifier() + 1;
	}
	
	default void lightning() {	
		setGenePool(getDefaultGenePool().randomizeAll());
		getEntity().attackEntityFrom(DamageSource.LIGHTNING_BOLT, 5.0F);
	}
	
	default void sendTo(PlayerEntity player) {
		player.sendStatusMessage(new StringTextComponent(getGenePool().toString()), true);
	}
}
