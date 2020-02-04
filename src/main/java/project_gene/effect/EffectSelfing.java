package project_gene.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.InstantEffect;
import project_gene.hook.VanillaHook;

import javax.annotation.Nullable;

public class EffectSelfing extends InstantEffect {
	public EffectSelfing() {
		super(EffectType.NEUTRAL, 0x9932CC);
	}
	
	@Override
	public void affectEntity(@Nullable Entity source, @Nullable Entity indirectSource, LivingEntity entityLivingBaseIn, int amplifier, double health) {
		VanillaHook.selfing(entityLivingBaseIn);
	}
}
