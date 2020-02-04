package project_gene.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.InstantEffect;

import javax.annotation.Nullable;

public class EffectLove extends InstantEffect {
	public EffectLove() {
		super(EffectType.NEUTRAL, 0xDC143C);
	}

	@Override
	public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier, double health) {
		if (entityLivingBaseIn instanceof AnimalEntity) {
			AnimalEntity animal = (AnimalEntity) entityLivingBaseIn;
			if (animal.getGrowingAge() == 0)
				animal.setInLove(indirectSource instanceof PlayerEntity ? (PlayerEntity) indirectSource : null);
		}
	}
}
