package project_gene.client.renderer.layer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.CowModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import project_gene.entity.living.Cow;

@OnlyIn(Dist.CLIENT)
public class CowMushroomLayer extends LayerRenderer<Cow, CowModel<Cow>> {
	public CowMushroomLayer(IEntityRenderer<Cow, CowModel<Cow>> p_i50931_1_) {
		super(p_i50931_1_);
	}

	public void render(Cow cow, float p_212842_2_, float p_212842_3_, float p_212842_4_, float p_212842_5_, float p_212842_6_, float p_212842_7_, float p_212842_8_) {
		Cow.MushroomType type = cow.getMushroomType();
		if (type != Cow.MushroomType.NONE && !cow.getSheared() && !cow.isChild() && !cow.isInvisible()) {
			int hash = cow.getUniqueID().hashCode();
			bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.enableCull();
			GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
			GlStateManager.pushMatrix();
			GlStateManager.scalef(1.0F, -1.0F, 1.0F);
			GlStateManager.translatef(0.2F, 0.35F, 0.5F);
			GlStateManager.rotatef(42.0F, 0.0F, 1.0F, 0.0F);
			BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
			GlStateManager.pushMatrix();
			GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
			dispatcher.renderBlockBrightness(cow.mushroom(hash, false).getDefaultState(), 1.0F);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translatef(0.1F, 0.0F, -0.6F);
			GlStateManager.rotatef(42.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
			dispatcher.renderBlockBrightness(cow.mushroom(~hash, false).getDefaultState(), 1.0F);
			GlStateManager.popMatrix();
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			getEntityModel().getHead().postRender(0.0625F);
			GlStateManager.scalef(1.0F, -1.0F, 1.0F);
			GlStateManager.translatef(0.0F, 0.7F, -0.2F);
			GlStateManager.rotatef(12.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translatef(-0.5F, -0.5F, 0.5F);
			dispatcher.renderBlockBrightness(cow.mushroom(0, true).getDefaultState(), 1.0F);
			GlStateManager.popMatrix();
			GlStateManager.cullFace(GlStateManager.CullFace.BACK);
			GlStateManager.disableCull();
		}
	}
	
	
	

	public boolean shouldCombineTextures() {
		return true;
	}
}