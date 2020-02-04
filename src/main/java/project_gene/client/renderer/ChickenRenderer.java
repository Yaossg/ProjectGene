package project_gene.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ChickenRenderer extends net.minecraft.client.renderer.entity.ChickenRenderer {
	public ChickenRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}


	@Override
	protected void preRenderCallback(ChickenEntity entitylivingbaseIn, float partialTickTime) {
		float scale = entitylivingbaseIn.getRenderScale();
		GlStateManager.scalef(scale, scale, scale);
	}
}
