package project_gene.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.passive.PigEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PigRenderer extends net.minecraft.client.renderer.entity.PigRenderer {
	public PigRenderer(EntityRendererManager renderManagerIn) {
		super(renderManagerIn);
	}


	@Override
	protected void preRenderCallback(PigEntity entitylivingbaseIn, float partialTickTime) {
		float scale = entitylivingbaseIn.getRenderScale();
		GlStateManager.scalef(scale, scale, scale);
	}
}
