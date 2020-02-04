package project_gene.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import project_gene.entity.core.IGenePooled;

public class EffectGene extends Effect {
	public EffectGene(int color) {
		super(EffectType.NEUTRAL, color);
	}

	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
		if (!(entityLivingBaseIn instanceof IGenePooled))
			entityLivingBaseIn.removePotionEffect(this);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}
}
