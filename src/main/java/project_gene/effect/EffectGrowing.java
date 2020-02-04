package project_gene.effect;

import net.minecraft.entity.AgeableEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;

public class EffectGrowing extends Effect {
	public EffectGrowing() {
		super(EffectType.NEUTRAL, 0x00FF7F);
	}

	@Override
	public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
		if (entityLivingBaseIn instanceof AgeableEntity) {
			AgeableEntity ageable = (AgeableEntity) entityLivingBaseIn;
			if (ageable.getGrowingAge() < 0) {
				ageable.addGrowth(amplifier + 1);
				return;
			}
		} 
		entityLivingBaseIn.removePotionEffect(this);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}
}
